package org.bxteam.ndailyrewards.listener;

import com.google.inject.Inject;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bxteam.ndailyrewards.configuration.PluginConfig;
import org.bxteam.ndailyrewards.manager.menu.MenuManager;
import org.bxteam.ndailyrewards.manager.reward.ActionsExecutor;
import org.bxteam.ndailyrewards.manager.reward.RewardManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryClickListener implements Listener {
    private final RewardManager rewardManager;
    private final ActionsExecutor.Factory actionsExecutorFactory;

    private final Map<Integer, Integer> slotToDayMap = new ConcurrentHashMap<>();
    private final Map<Integer, List<String>> customButtonActions = new ConcurrentHashMap<>();

    @Inject
    public InventoryClickListener(PluginConfig config, RewardManager rewardManager, ActionsExecutor.Factory actionsExecutorFactory) {
        this.rewardManager = rewardManager;
        this.actionsExecutorFactory = actionsExecutorFactory;

        config.rewards.days.forEach((day, reward) -> slotToDayMap.put(reward.position, day));
        config.gui.reward.custom.values().forEach(item -> customButtonActions.put(item.position, item.actions));
    }

    @EventHandler
    public void mainMenuClickListener(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuManager.MainMenuHolder)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        Integer day = slotToDayMap.get(slot);
        if (day != null && day > 0) {
            this.rewardManager.handleReward(player, day);
            return;
        }

        List<String> actions = customButtonActions.get(slot);
        if (actions != null && !actions.isEmpty()) {
            this.actionsExecutorFactory.create(player, actions).execute();
        }
    }
}
