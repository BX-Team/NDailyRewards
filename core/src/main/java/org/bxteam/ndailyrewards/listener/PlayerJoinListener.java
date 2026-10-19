package org.bxteam.ndailyrewards.listener;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bxteam.ndailyrewards.api.event.AutoClaimEvent;
import org.bxteam.ndailyrewards.api.event.PlayerReceiveReminderEvent;
import org.bxteam.ndailyrewards.configuration.Messages;
import org.bxteam.ndailyrewards.configuration.PluginConfig;
import org.bxteam.ndailyrewards.event.EventCaller;
import org.bxteam.ndailyrewards.manager.menu.MenuManager;
import org.bxteam.ndailyrewards.manager.reward.RewardManager;
import org.bxteam.ndailyrewards.messaging.MessageService;
import org.bxteam.quark.common.QuarkLogger;
import org.bxteam.quark.paper.PaperScheduler;

import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerJoinListener implements Listener {
    private final PluginConfig config;
    private final Messages messages;
    private final RewardManager rewardManager;
    private final MenuManager menuManager;
    private final QuarkLogger logger;
    private final PaperScheduler scheduler;
    private final EventCaller eventCaller;
    private final MessageService messageService;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        this.rewardManager.createInitialPlayerData(uuid)
                .thenAccept(initialData -> checkPlayerRewards(player))
                .exceptionally(throwable -> {
                    this.logger.error("Could not create initial player data", throwable);
                    return null;
                });
    }

    private void checkPlayerRewards(Player player) {
        this.rewardManager.getPlayerRewardDataAsync(player.getUniqueId()).thenAccept(playerRewardData -> {
            if (playerRewardData == null) return;

            int currentDay = playerRewardData.currentDay() + 1;

            if (this.rewardManager.isRewardAvailable(playerRewardData, currentDay)) {
                this.rewardManager.checkResetForPlayerAsync(player.getUniqueId()).thenAccept(wasReset -> {
                    if (wasReset) {
                        messageService.send(player, messages.claim.rewardReset);
                        return;
                    }

                    PluginConfig.Events events = this.config.events;
                    if (events.autoClaimReward) {
                        this.scheduler.later(player, () -> {
                            this.rewardManager.giveReward(player, currentDay);
                            this.eventCaller.callEvent(new AutoClaimEvent(player, currentDay));
                        }, Duration.ofSeconds(events.autoClaimDelay));
                    }

                    if (events.openGuiWhenAvailable) {
                        this.scheduler.sync(player, () -> {
                            this.menuManager.openRewardsMenu(player);
                            if (this.config.sound.open.enabled) {
                                this.config.sound.open.type.play(player);
                            }
                        });
                    }

                    if (events.notifyWhenAvailable) {
                        messageService.send(player, messages.events.notifyWhenAvailable);
                        this.eventCaller.callEvent(new PlayerReceiveReminderEvent(player, currentDay));
                    }
                });
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.rewardManager.invalidateCache(event.getPlayer().getUniqueId());
    }
}
