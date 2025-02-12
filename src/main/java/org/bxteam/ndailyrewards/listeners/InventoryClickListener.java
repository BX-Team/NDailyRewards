package org.bxteam.ndailyrewards.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.managers.MenuManager;
import org.bxteam.ndailyrewards.managers.reward.RewardManager;
import org.bxteam.ndailyrewards.utils.TextUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optimized InventoryClickListener:
 * - Utilizes a precomputed slot-to-day map for faster lookups.
 * - Reduces configuration access by caching actions per custom button.
 */
public class InventoryClickListener implements Listener {
    // Precomputed map of slot positions to day numbers
    private final Map<Integer, Integer> slotToDayMap = new ConcurrentHashMap<>();

    // Precomputed map of custom button slots to their actions
    private final Map<Integer, List<String>> customButtonActions = new ConcurrentHashMap<>();

    public InventoryClickListener() {
        initializeMappings();
    }

    /**
     * Initializes slot-to-day and custom button action mappings.
     */
    private void initializeMappings() {
        final NDailyRewards instance = NDailyRewards.getInstance();
        ConfigurationSection daysSection = instance.getConfig().getConfigurationSection("rewards.days");
        if (daysSection != null) {
            for (String dayKey : daysSection.getKeys(false)) {
                int day = Integer.parseInt(dayKey);
                ConfigurationSection daySection = daysSection.getConfigurationSection(dayKey);
                if (daySection != null) {
                    int position = daySection.getInt("position");
                    slotToDayMap.put(position, day);
                }
            }
        }

        ConfigurationSection customSection = instance.getConfig().getConfigurationSection("gui.reward.custom");
        if (customSection != null) {
            for (String customKey : customSection.getKeys(false)) {
                int position = customSection.getInt(customKey + ".position");
                List<String> actions = customSection.getStringList(customKey + ".actions");
                customButtonActions.put(position, actions);
            }
        }
    }

    @EventHandler
    public void mainMenuClickListener(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuManager.MainMenuHolder)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        RewardManager rewardManager = NDailyRewards.getInstance().getRewardManager();

        // Check if the clicked slot corresponds to a day
        Integer day = slotToDayMap.get(slot);
        if (day != null && day > 0) {
            rewardManager.handleReward(player, day);
            return;
        }

        // Handle custom button clicks
        List<String> actions = customButtonActions.get(slot);
        if (actions != null && !actions.isEmpty()) {
            executeActions(player, actions);
        }
    }

    /**
     * Executes a list of actions defined in the configuration.
     * @param player The player to execute actions for.
     * @param actions The list of action strings.
     */
    private void executeActions(Player player, List<String> actions) {
        for (String action : actions) {
            if (action.startsWith("[console]")) {
                String command = action.substring("[console]".length()).trim();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            } else if (action.startsWith("[player]")) {
                String command = action.substring("[player]".length()).trim();
                player.performCommand(command);
            } else if (action.startsWith("[message]")) {
                String message = action.substring("[message]".length()).trim();
                player.sendMessage(TextUtils.applyColor(message));
            } else if (action.startsWith("[actionbar]")) {
                String actionBarMessage = action.substring("[actionbar]".length()).trim();
                player.sendActionBar(TextUtils.applyColor(actionBarMessage));
            } else if (action.startsWith("[sound]")) {
                String[] soundParams = action.substring("[sound]".length()).trim().split(":");
                if (soundParams.length >= 1) {
                    try {
                        Sound sound = Sound.valueOf(soundParams[0].toUpperCase());
                        float volume = soundParams.length > 1 ? Float.parseFloat(soundParams[1]) : 1.0f;
                        float pitch = soundParams.length > 2 ? Float.parseFloat(soundParams[2]) : 1.0f;
                        player.playSound(player.getLocation(), sound, volume, pitch);
                    } catch (IllegalArgumentException e) {
                        // Invalid sound, ignore or log as needed
                        player.sendMessage(TextUtils.applyColor("&cInvalid sound specified: " + soundParams[0]));
                    }
                }
            } else if (action.startsWith("[close]")) {
                player.closeInventory();
            }
        }
    }
}