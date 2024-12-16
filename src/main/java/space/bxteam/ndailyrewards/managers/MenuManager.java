package space.bxteam.ndailyrewards.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.managers.reward.PlayerRewardData;
import space.bxteam.ndailyrewards.managers.reward.RewardManager;
import space.bxteam.ndailyrewards.utils.ItemBuilder;
import space.bxteam.ndailyrewards.utils.TextUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Optimized MenuManager:
 * - We now cache all static items instead of rebuilding them every tick.
 * - The "next" items are also cached, and we only update their lore's time each second.
 * - This avoids expensive operations like fromStringToJSON on every tick.
 * - Overall, this should drastically reduce lag.
 */
public class MenuManager {
    private final InventoryHolder MAIN_MENU_HOLDER = new MainMenuHolder();

    // Cached filler and prebuilt items for each day
    private ItemStack cachedFillerItem;
    private final Map<Integer, DayItems> dayItemsCache = new HashMap<>();
    private final List<CustomItemConfig> cachedCustomItems = new ArrayList<>();

    /**
     * Opens the rewards menu for a player, with significantly reduced overhead.
     * Instead of rebuilding items every tick, we build them once and only update what's strictly necessary.
     */
    public void openRewardsMenu(Player player) {
        final NDailyRewards instance = NDailyRewards.getInstance();
        final ConfigurationSection config = instance.getConfig();

        int size = config.getInt("gui.reward.size");
        String title = TextUtils.applyColor(config.getString("gui.reward.title"));
        boolean useFiller = config.getBoolean("gui.reward.display.filler.enable");

        Bukkit.getScheduler().runTask(instance, () -> {
            final Inventory inventory = Bukkit.createInventory(MAIN_MENU_HOLDER, size, title);

            // If we use filler items, cache it once and fill the empty slots
            if (useFiller) {
                if (this.cachedFillerItem == null) {
                    this.cachedFillerItem = loadFillItem();
                }
                for (int i = 0; i < size; i++) {
                    inventory.setItem(i, this.cachedFillerItem);
                }
            }

            // Cache custom items once
            ConfigurationSection customSection = config.getConfigurationSection("gui.reward.custom");
            if (customSection != null && cachedCustomItems.isEmpty()) {
                for (String customKey : customSection.getKeys(false)) {
                    String materialStr = customSection.getString(customKey + ".material");
                    String name = customSection.getString(customKey + ".name");
                    List<String> lore = customSection.getStringList(customKey + ".lore");
                    int position = customSection.getInt(customKey + ".position");
                    ItemStack customItem = new ItemBuilder(ItemBuilder.parseItemStack(materialStr))
                            .setName(name)
                            .setLore(lore)
                            .build();
                    cachedCustomItems.add(new CustomItemConfig(position, customItem));
                }
            }

            ConfigurationSection daysSection = config.getConfigurationSection("rewards.days");
            if (daysSection != null) {
                RewardManager rewardManager = instance.getRewardManager();
                PlayerRewardData playerRewardData = rewardManager.getPlayerRewardData(player.getUniqueId());

                // Pre-build and cache items for each day so we don't rebuild them every second
                if (dayItemsCache.isEmpty()) {
                    for (String dayKey : daysSection.getKeys(false)) {
                        int day = Integer.parseInt(dayKey);
                        ConfigurationSection daySection = daysSection.getConfigurationSection(dayKey);
                        if (daySection == null) continue;
                        dayItemsCache.put(day, preCacheDayItems(day, daySection));
                    }
                }

                // Schedule an update task every second to only refresh what needs updating
                final AtomicReference<BukkitTask> task = new AtomicReference<>();
                task.set(Bukkit.getScheduler().runTaskTimer(instance, () -> {
                    if (!(inventory.getHolder() instanceof MainMenuHolder)) {
                        // If the holder changed (player closed inv?), stop updating
                        BukkitTask t = task.get();
                        if (t != null) t.cancel();
                        return;
                    }

                    long currentTime = System.currentTimeMillis() / 1000L;
                    int currentDay = playerRewardData.currentDay();
                    long nextTime = playerRewardData.next();

                    // For each day, just pick the right pre-built item. If "next", just update the time left.
                    for (Map.Entry<Integer, DayItems> entry : dayItemsCache.entrySet()) {
                        int day = entry.getKey();
                        DayItems cached = entry.getValue();

                        ItemStack toSet;
                        if (currentDay >= day) {
                            // Already claimed
                            toSet = cached.claimed;
                        } else if (currentDay + 1 == day && currentTime >= nextTime) {
                            // Available to claim now
                            toSet = cached.available;
                        } else if (currentDay + 1 == day && currentTime < nextTime) {
                            // "Next" day not yet available, update time left
                            toSet = updateNextItemTime(cached.nextTemplate, nextTime - currentTime);
                        } else {
                            // Not available yet, not next, just unavailable
                            toSet = cached.unavailable;
                        }

                        inventory.setItem(cached.position, toSet);
                    }

                    // Custom items are static, just set them once (or remove this if we don't need to do it every tick)
                    for (CustomItemConfig cic : cachedCustomItems) {
                        inventory.setItem(cic.position, cic.itemStack);
                    }

                }, 0L, 20L));
            } else {
                // If no days are configured, just place custom items
                for (CustomItemConfig cic : cachedCustomItems) {
                    inventory.setItem(cic.position, cic.itemStack);
                }
            }

            player.openInventory(inventory);
        });
    }

    /**
     * Load the filler item once.
     */
    private ItemStack loadFillItem() {
        final NDailyRewards instance = NDailyRewards.getInstance();
        final String material = instance.getConfig().getString("gui.reward.display.filler.material");
        final String name = instance.getConfig().getString("gui.reward.display.filler.name");
        final List<String> lore = instance.getConfig().getStringList("gui.reward.display.filler.lore");

        return new ItemBuilder(ItemBuilder.parseItemStack(Objects.requireNonNull(material)))
                .setName(name)
                .setLore(lore)
                .build();
    }

    /**
     * Pre-build all items related to a specific day: claimed, available, unavailable, and the next template.
     * This avoids rebuilding them every tick.
     */
    private DayItems preCacheDayItems(int day, ConfigurationSection daySection) {
        int position = daySection.getInt("position");
        List<String> rewardLore = daySection.getStringList("lore").stream()
                .map(TextUtils::applyColor)
                .collect(Collectors.toList());

        ItemStack claimed = createStaticItem("claimed", day, rewardLore);
        ItemStack available = createStaticItem("available", day, rewardLore);
        ItemStack unavailable = createStaticItem("unavailable", day, rewardLore);
        // Build a template for "next" once; we'll just update the time string each tick
        ItemStack nextTemplate = createNextTemplate(day, rewardLore);

        return new DayItems(position, claimed, available, unavailable, nextTemplate);
    }

    /**
     * Create static items (claimed, available, unavailable) once.
     */
    private ItemStack createStaticItem(String type, int day, List<String> rewardLore) {
        final NDailyRewards instance = NDailyRewards.getInstance();
        String material = instance.getConfig().getString("gui.reward.display." + type + ".material");
        String name = Objects.requireNonNull(instance.getConfig().getString("gui.reward.display." + type + ".name"))
                .replace("<dayNum>", String.valueOf(day));

        List<String> loreFormatted = instance.getConfig().getStringList("gui.reward.display." + type + ".lore").stream()
                .map(s -> s.replace("<reward-lore>", String.join("\n", rewardLore)))
                .flatMap(s -> Arrays.stream(s.split("\n")))
                .map(TextUtils::applyColor)
                .collect(Collectors.toList());

        return new ItemBuilder(ItemBuilder.parseItemStack(Objects.requireNonNull(material)))
                .setName(name)
                .setLore(loreFormatted)
                .build();
    }

    /**
     * Create a "next" item template.
     * We leave "<time-left>" as a placeholder in the lore and just replace it each tick later.
     */
    private ItemStack createNextTemplate(int day, List<String> rewardLore) {
        final NDailyRewards instance = NDailyRewards.getInstance();
        String material = instance.getConfig().getString("gui.reward.display.next.material");
        String name = Objects.requireNonNull(instance.getConfig().getString("gui.reward.display.next.name"))
                .replace("<dayNum>", String.valueOf(day));

        List<String> loreTemplate = instance.getConfig().getStringList("gui.reward.display.next.lore").stream()
                .map(s -> s.replace("<reward-lore>", String.join("\n", rewardLore)))
                .flatMap(s -> Arrays.stream(s.split("\n")))
                .map(TextUtils::applyColor)
                .collect(Collectors.toList());

        return new ItemBuilder(ItemBuilder.parseItemStack(Objects.requireNonNull(material)))
                .setName(name)
                .setLore(loreTemplate)
                .build();
    }

    /**
     * Update the "<time-left>" value in the next item without rebuilding everything.
     */
    private ItemStack updateNextItemTime(ItemStack template, long timeLeft) {
        ItemStack copy = template.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta == null) return copy;

        List<String> lore = meta.getLore();
        if (lore == null) return copy;

        String formattedTime = formatTime(timeLeft);
        List<String> newLore = new ArrayList<>(lore.size());
        for (String line : lore) {
            newLore.add(line.replace("<time-left>", formattedTime));
        }
        meta.setLore(newLore);
        copy.setItemMeta(meta);
        return copy;
    }

    /**
     * Format seconds into HH:MM:SS.
     */
    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /**
     * Holder used to identify the main menu, so we can stop the task when it's closed.
     */
    public static class MainMenuHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }
    }

    private record CustomItemConfig(int position, ItemStack itemStack) {}

    private record DayItems(int position, ItemStack claimed, ItemStack available, ItemStack unavailable, ItemStack nextTemplate) {}
}