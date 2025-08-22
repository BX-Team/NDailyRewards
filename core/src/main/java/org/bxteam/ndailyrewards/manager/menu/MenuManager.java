package org.bxteam.ndailyrewards.manager.menu;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bxteam.helix.scheduler.Scheduler;
import org.bxteam.helix.scheduler.Task;
import org.bxteam.ndailyrewards.configuration.Language;
import org.jetbrains.annotations.NotNull;
import org.bxteam.ndailyrewards.manager.reward.PlayerRewardData;
import org.bxteam.ndailyrewards.manager.reward.RewardManager;
import org.bxteam.ndailyrewards.utils.ItemBuilder;
import org.bxteam.ndailyrewards.utils.TextUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class MenuManager {
    private final Plugin plugin;
    private final RewardManager rewardManager;
    private final Scheduler scheduler;

    private final InventoryHolder MAIN_MENU_HOLDER = new MainMenuHolder();
    private ItemStack cachedFillerItem;
    private final Map<Integer, DayItems> dayItemsCache = new HashMap<>();
    private final List<CustomItemConfig> cachedCustomItems = new ArrayList<>();
    private final Map<Integer, Integer> slotToDayMap = new HashMap<>();
    private final Set<Player> openMenuPlayers = ConcurrentHashMap.newKeySet();

    private Task updateTask;

    @Inject
    public MenuManager(Plugin plugin, RewardManager rewardManager, Scheduler scheduler) {
        this.plugin = plugin;
        this.rewardManager = rewardManager;
        this.scheduler = scheduler;

        initializeCaches();
        startGlobalUpdateTask();
    }

    private void initializeCaches() {
        final ConfigurationSection config = plugin.getConfig();

        // Cache filler item
        if (config.getBoolean("gui.reward.display.filler.enable") && this.cachedFillerItem == null) {
            this.cachedFillerItem = loadFillItem();
        }

        // Cache custom items
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

        // Cache day items and slot-to-day mappings
        ConfigurationSection daysSection = config.getConfigurationSection("rewards.days");
        if (daysSection != null && dayItemsCache.isEmpty()) {
            for (String dayKey : daysSection.getKeys(false)) {
                int day = Integer.parseInt(dayKey);
                ConfigurationSection daySection = daysSection.getConfigurationSection(dayKey);
                if (daySection == null) continue;
                DayItems dayItems = preCacheDayItems(day, daySection);
                dayItemsCache.put(day, dayItems);
                slotToDayMap.put(dayItems.position, day);
            }
        }
    }

    private void startGlobalUpdateTask() {
        updateTask = scheduler.runTaskTimer(() -> {
            long currentTime = System.currentTimeMillis() / 1000L;
            for (Player player : openMenuPlayers) {
                updatePlayerInventory(player, currentTime);
            }
        }, 20L, 20L);
    }

    public void openRewardsMenu(Player player) {
        final ConfigurationSection config = plugin.getConfig();

        this.rewardManager.checkResetForPlayerAsync(player.getUniqueId())
            .thenCompose(wasReset -> {
                if (wasReset) {
                    scheduler.runTask(() -> player.sendMessage(Language.PREFIX.asColoredString() + Language.CLAIM_REWARD_RESET.asColoredString()));
                }

                return this.rewardManager.getPlayerRewardDataAsync(player.getUniqueId());
            })
            .thenAccept(playerRewardData -> {
                int size = config.getInt("gui.reward.size");
                String title = TextUtils.applyColor(config.getString("gui.reward.title"));
                boolean useFiller = config.getBoolean("gui.reward.display.filler.enable");

                scheduler.runTask(() -> {
                    final Inventory inventory = Bukkit.createInventory(MAIN_MENU_HOLDER, size, title);

                    if (useFiller && cachedFillerItem != null) {
                        for (int i = 0; i < size; i++) {
                            inventory.setItem(i, cachedFillerItem);
                        }
                    }

                    for (CustomItemConfig cic : cachedCustomItems) {
                        inventory.setItem(cic.position, cic.itemStack);
                    }

                    ConfigurationSection daysSection = config.getConfigurationSection("rewards.days");
                    if (daysSection != null && playerRewardData != null) {
                        populateDayItems(inventory, playerRewardData, System.currentTimeMillis() / 1000L);
                        openMenuPlayers.add(player);
                    }

                    player.openInventory(inventory);
                });
            });
    }

    private void populateDayItems(Inventory inventory, PlayerRewardData playerRewardData, long currentTime) {
        int currentDay = playerRewardData.currentDay();
        long nextTime = playerRewardData.next();

        for (DayItems cached : dayItemsCache.values()) {
            ItemStack toSet;
            if (currentDay >= cached.day) {
                // Already claimed
                toSet = cached.claimed;
            } else if (currentDay + 1 == cached.day && currentTime >= nextTime) {
                // Available to claim now
                toSet = cached.available;
            } else if (currentDay + 1 == cached.day && currentTime < nextTime) {
                // "Next" day not yet available, update time left
                toSet = updateNextItemTime(cached.nextTemplate, nextTime - currentTime);
            } else {
                // Not available yet, not next, just unavailable
                toSet = cached.unavailable;
            }

            inventory.setItem(cached.position, toSet);
        }
    }

    private void updatePlayerInventory(Player player, long currentTime) {
        if (!player.isOnline()) {
            openMenuPlayers.remove(player);
            return;
        }

        Inventory inventory = player.getOpenInventory().getTopInventory();
        if (!(inventory.getHolder() instanceof MainMenuHolder)) {
            openMenuPlayers.remove(player);
            return;
        }

        this.rewardManager.getPlayerRewardDataAsync(player.getUniqueId())
            .thenAccept(playerRewardData -> {
                if (playerRewardData != null) {
                    scheduler.runTask(() -> populateDayItems(inventory, playerRewardData, currentTime));
                }
            });
    }

    private ItemStack loadFillItem() {
        final String material = plugin.getConfig().getString("gui.reward.display.filler.material");
        final String name = plugin.getConfig().getString("gui.reward.display.filler.name");
        final List<String> lore = plugin.getConfig().getStringList("gui.reward.display.filler.lore");

        return new ItemBuilder(ItemBuilder.parseItemStack(Objects.requireNonNull(material)))
                .setName(name)
                .setLore(lore)
                .build();
    }

    private DayItems preCacheDayItems(int day, ConfigurationSection daySection) {
        int position = daySection.getInt("position");
        List<String> rewardLore = daySection.getStringList("lore").stream()
                .map(TextUtils::applyColor)
                .collect(Collectors.toList());

        ItemStack claimed = createStaticItem("claimed", day, rewardLore);
        ItemStack available = createStaticItem("available", day, rewardLore);
        ItemStack unavailable = createStaticItem("unavailable", day, rewardLore);
        // Build a template for "next" once; we'll just update the time string each tick later
        ItemStack nextTemplate = createNextTemplate(day, rewardLore);

        return new DayItems(day, position, claimed, available, unavailable, nextTemplate);
    }

    private ItemStack createStaticItem(String type, int day, List<String> rewardLore) {
        String material = plugin.getConfig().getString("gui.reward.display." + type + ".material");
        String name = Objects.requireNonNull(plugin.getConfig().getString("gui.reward.display." + type + ".name"))
                .replace("<dayNum>", String.valueOf(day));

        List<String> loreFormatted = plugin.getConfig().getStringList("gui.reward.display." + type + ".lore").stream()
                .map(s -> s.replace("<reward-lore>", String.join("\n", rewardLore)))
                .flatMap(s -> Arrays.stream(s.split("\n")))
                .map(TextUtils::applyColor)
                .collect(Collectors.toList());

        return new ItemBuilder(ItemBuilder.parseItemStack(Objects.requireNonNull(material)))
                .setName(name)
                .setLore(loreFormatted)
                .build();
    }

    private ItemStack createNextTemplate(int day, List<String> rewardLore) {
        String material = plugin.getConfig().getString("gui.reward.display.next.material");
        String name = Objects.requireNonNull(plugin.getConfig().getString("gui.reward.display.next.name"))
                .replace("<dayNum>", String.valueOf(day));

        List<String> loreTemplate = plugin.getConfig().getStringList("gui.reward.display.next.lore").stream()
                .map(s -> s.replace("<reward-lore>", String.join("\n", rewardLore)))
                .flatMap(s -> Arrays.stream(s.split("\n")))
                .map(TextUtils::applyColor)
                .collect(Collectors.toList());

        return new ItemBuilder(ItemBuilder.parseItemStack(Objects.requireNonNull(material)))
                .setName(name)
                .setLore(loreTemplate)
                .build();
    }

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

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    public void shutdown() {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
        }
        openMenuPlayers.clear();
        dayItemsCache.clear();
        cachedCustomItems.clear();
        slotToDayMap.clear();
        cachedFillerItem = null;
    }

    public void refreshPlayerInventory(Player player) {
        if (!player.isOnline()) {
            openMenuPlayers.remove(player);
            return;
        }

        Inventory inventory = player.getOpenInventory().getTopInventory();
        if (!(inventory.getHolder() instanceof MainMenuHolder)) {
            openMenuPlayers.remove(player);
            return;
        }

        this.rewardManager.getPlayerRewardDataAsync(player.getUniqueId())
            .thenAccept(playerRewardData -> {
                if (playerRewardData != null) {
                    scheduler.runTask(() -> {
                        long currentTime = System.currentTimeMillis() / 1000L;
                        populateDayItems(inventory, playerRewardData, currentTime);
                    });
                }
            });
    }

    public static class MainMenuHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return Bukkit.createInventory(this, 0, "");
        }
    }

    private record CustomItemConfig(int position, ItemStack itemStack) {}

    private record DayItems(int day, int position, ItemStack claimed, ItemStack available, ItemStack unavailable, ItemStack nextTemplate) {}
}
