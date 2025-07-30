package org.bxteam.ndailyrewards.listener;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;
import org.bxteam.ndailyrewards.manager.menu.MenuManager;
import org.bxteam.ndailyrewards.manager.reward.RewardManager;
import org.bxteam.ndailyrewards.utils.TextUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryClickListener implements Listener {
    private final Plugin plugin;
    private final RewardManager rewardManager;

    private final Map<Integer, Integer> slotToDayMap = new ConcurrentHashMap<>();
    private final Map<Integer, List<String>> customButtonActions = new ConcurrentHashMap<>();

    @Inject
    public InventoryClickListener(Plugin plugin, RewardManager rewardManager) {
        this.plugin = plugin;
        this.rewardManager = rewardManager;
        initializeMappings();
    }

    private void initializeMappings() {
        ConfigurationSection daysSection = this.plugin.getConfig().getConfigurationSection("rewards.days");
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

        ConfigurationSection customSection = this.plugin.getConfig().getConfigurationSection("gui.reward.custom");
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

        // Check if the clicked slot corresponds to a day
        Integer day = slotToDayMap.get(slot);
        if (day != null && day > 0) {
            this.rewardManager.handleReward(player, day);
            return;
        }

        // Handle custom button clicks
        List<String> actions = customButtonActions.get(slot);
        if (actions != null && !actions.isEmpty()) {
            executeActions(player, actions);
        }
    }

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
                        player.sendMessage(TextUtils.applyColor("&cInvalid sound specified: " + soundParams[0]));
                    }
                }
            } else if (action.startsWith("[close]")) {
                player.closeInventory();
            }
        }
    }
}
