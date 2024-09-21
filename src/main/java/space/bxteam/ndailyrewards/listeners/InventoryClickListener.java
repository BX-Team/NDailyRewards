package space.bxteam.ndailyrewards.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.managers.MenuManager;
import space.bxteam.ndailyrewards.managers.reward.RewardManager;
import space.bxteam.ndailyrewards.utils.TextUtils;

import java.util.List;

public class InventoryClickListener implements Listener {
    @EventHandler
    public void mainMenuClickListener(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuManager.MainMenuHolder)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        RewardManager rewardManager = NDailyRewards.getInstance().getRewardManager();
        int day = getDayFromSlot(slot);

        if (day > 0) {
            rewardManager.handleReward(player, day);
        } else {
            handleCustomButtonClick(player, slot);
        }
    }

    private void handleCustomButtonClick(Player player, int slot) {
        ConfigurationSection customSection = NDailyRewards.getInstance().getConfig().getConfigurationSection("gui.reward.custom");
        if (customSection != null) {
            for (String customKey : customSection.getKeys(false)) {
                int position = customSection.getInt(customKey + ".position");
                if (position == slot) {
                    List<String> actions = customSection.getStringList(customKey + ".actions");
                    executeActions(player, actions);
                    break;
                }
            }
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
                    Sound sound = Sound.valueOf(soundParams[0].toUpperCase());
                    float volume = soundParams.length > 1 ? Float.parseFloat(soundParams[1]) : 1.0f;
                    float pitch = soundParams.length > 2 ? Float.parseFloat(soundParams[2]) : 1.0f;
                    player.playSound(player.getLocation(), sound, volume, pitch);
                }
            } else if (action.startsWith("[close]")) {
                player.closeInventory();
            }
        }
    }

    private int getDayFromSlot(int slot) {
        ConfigurationSection daysSection = NDailyRewards.getInstance().getConfig().getConfigurationSection("rewards.days");
        if (daysSection != null) {
            for (String dayKey : daysSection.getKeys(false)) {
                int day = Integer.parseInt(dayKey);
                ConfigurationSection daySection = daysSection.getConfigurationSection(dayKey);
                if (daySection != null) {
                    int position = daySection.getInt("position");
                    if (position == slot) {
                        return day;
                    }
                }
            }
        }
        return -1;
    }
}
