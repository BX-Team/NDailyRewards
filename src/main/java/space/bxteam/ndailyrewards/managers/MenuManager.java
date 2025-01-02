package space.bxteam.ndailyrewards.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.managers.reward.PlayerRewardData;
import space.bxteam.ndailyrewards.managers.reward.RewardManager;
import space.bxteam.ndailyrewards.utils.ItemBuilder;
import space.bxteam.ndailyrewards.utils.TextUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Optimized MenuManager:
 * - Implements a singleton pattern to ensure a single instance.
 * - Uses a global scheduled task to update all open inventories, reducing the number of concurrent tasks.
 * - Caches slot-to-day mappings for faster lookup.
 * - Minimizes cloning of ItemStacks by reusing cached items.
 */
public class MenuManager {
    private static MenuManager instance;

    private final InventoryHolder MAIN_MENU_HOLDER = new MainMenuHolder();

    // Cached filler and prebuilt items for each day
    private ItemStack cachedFillerItem;
    private final Map<Integer, DayItems> dayItemsCache = new HashMap<>();
    private final List<CustomItemConfig> cachedCustomItems = new ArrayList<>();

    // Mapping of slot positions to day numbers for quick lookup
    private final Map<Integer, Integer> slotToDayMap = new HashMap<>();

    // Set of players who currently have the rewards menu open
    private final Set<Player> openMenuPlayers = ConcurrentHashMap.newKeySet();

    // Global scheduled task reference
    private BukkitTask updateTask;

    public MenuManager() {
        initializeCaches();
        startGlobalUpdateTask();
    }

    /**
     * Singleton instance retrieval.
     */
    public static synchronized MenuManager getInstance() {
        if (instance == null) {
            instance = new MenuManager();
        }
        return instance;
    }

    /**
     * Initialize static caches for filler, custom items, and day items.
     */
    private void initializeCaches() {
        final NDailyRewards instance = NDailyRewards.getInstance();
        final ConfigurationSection config = instance.getConfig();

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
            RewardManager rewardManager = instance.getRewardManager();
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

    /**
     * Starts a global scheduled task that updates all open reward menus every second.
     */
    private void startGlobalUpdateTask() {
        final NDailyRewards instance = NDailyRewards.getInstance();
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis() / 1000L;
                for (Player player : openMenuPlayers) {
                    updatePlayerInventory(player, currentTime);
                }
            }
        }.runTaskTimer(instance, 20L, 20L); // Run every second
    }

    /**
     * Opens the rewards menu for a player.
     * @param player The player to open the menu for.
     */
    public void openRewardsMenu(Player player) {
        final NDailyRewards instance = NDailyRewards.getInstance();
        final ConfigurationSection config = instance.getConfig();

        int size = config.getInt("gui.reward.size");
        String title = TextUtils.applyColor(config.getString("gui.reward.title"));
        boolean useFiller = config.getBoolean("gui.reward.display.filler.enable");

        final Inventory inventory = Bukkit.createInventory(MAIN_MENU_HOLDER, size, title);

        // Fill inventory with filler items if enabled
        if (useFiller && cachedFillerItem != null) {
            for (int i = 0; i < size; i++) {
                inventory.setItem(i, cachedFillerItem);
            }
        }

        // Place custom items
        for (CustomItemConfig cic : cachedCustomItems) {
            inventory.setItem(cic.position, cic.itemStack);
        }

        // Initialize day items
        ConfigurationSection daysSection = config.getConfigurationSection("rewards.days");
        if (daysSection != null) {
            RewardManager rewardManager = instance.getRewardManager();
            PlayerRewardData playerRewardData = rewardManager.getPlayerRewardData(player.getUniqueId());

            // Initial population of day items
            populateDayItems(inventory, playerRewardData, System.currentTimeMillis() / 1000L);

            // Track the player for inventory updates
            openMenuPlayers.add(player);
        }

        // Open the inventory for the player
        player.openInventory(inventory);
    }

    /**
     * Populates the day items in the inventory based on the player's reward data.
     * @param inventory The inventory to populate.
     * @param playerRewardData The player's reward data.
     * @param currentTime The current server time in seconds.
     */
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

    /**
     * Updates the inventory of a player with the current time.
     * @param player The player whose inventory is to be updated.
     * @param currentTime The current server time in seconds.
     */
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

        final NDailyRewards instance = NDailyRewards.getInstance();
        RewardManager rewardManager = instance.getRewardManager();
        PlayerRewardData playerRewardData = rewardManager.getPlayerRewardData(player.getUniqueId());

        int currentDay = playerRewardData.currentDay();
        long nextTime = playerRewardData.next();

        for (DayItems cached : dayItemsCache.values()) {
            ItemStack toSet;
            if (currentDay >= cached.day) {
                toSet = cached.claimed;
            } else if (currentDay + 1 == cached.day && currentTime >= nextTime) {
                toSet = cached.available;
            } else if (currentDay + 1 == cached.day && currentTime < nextTime) {
                toSet = updateNextItemTime(cached.nextTemplate, nextTime - currentTime);
            } else {
                toSet = cached.unavailable;
            }

            inventory.setItem(cached.position, toSet);
        }
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
     * @param day The day number.
     * @param daySection The configuration section for the day.
     * @return A DayItems object containing all related ItemStacks.
     */
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
     * @param template The cached next template item.
     * @param timeLeft The time left in seconds.
     * @return A new ItemStack with updated time lore.
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
     * @param seconds The number of seconds to format.
     * @return A formatted time string.
     */
    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /**
     * Removes a player from the tracking set when they close the inventory.
     * Should be called from an InventoryCloseEvent listener.
     * @param player The player who closed the inventory.
     */
    public void removePlayer(Player player) {
        openMenuPlayers.remove(player);
    }

    /**
     * Holder used to identify the main menu, so we can manage updates.
     */
    public static class MainMenuHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return Bukkit.createInventory(this, 0, ""); // Placeholder, actual inventory is managed elsewhere
        }
    }

    private record CustomItemConfig(int position, ItemStack itemStack) {}

    private record DayItems(int day, int position, ItemStack claimed, ItemStack available, ItemStack unavailable, ItemStack nextTemplate) {}
}