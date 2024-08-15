package space.bxteam.ndailyrewards.listeners;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.managers.MenuManager;
import space.bxteam.ndailyrewards.managers.reward.RewardManager;

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
