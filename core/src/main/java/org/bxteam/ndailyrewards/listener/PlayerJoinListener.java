package org.bxteam.ndailyrewards.listener;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bxteam.helix.logger.ExtendedLogger;
import org.bxteam.helix.scheduler.Scheduler;
import org.bxteam.helix.updater.VersionFetcher;
import org.bxteam.ndailyrewards.event.EventCaller;
import org.bxteam.ndailyrewards.manager.menu.MenuManager;
import org.bxteam.ndailyrewards.utils.SoundUtil;
import org.bxteam.ndailyrewards.utils.TextUtils;
import org.bxteam.ndailyrewards.api.event.AutoClaimEvent;
import org.bxteam.ndailyrewards.api.event.PlayerReceiveReminderEvent;
import org.bxteam.ndailyrewards.configuration.Language;
import org.bxteam.ndailyrewards.manager.reward.RewardManager;

import java.util.Objects;
import java.util.UUID;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerJoinListener implements Listener {
    private final Plugin plugin;
    private final RewardManager rewardManager;
    private final MenuManager menuManager;
    private final ExtendedLogger logger;
    private final VersionFetcher versionFetcher;
    private final Scheduler scheduler;
    private final EventCaller eventCaller;
    private final SoundUtil soundUtil;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        this.rewardManager.createInitialPlayerData(uuid)
                .thenAccept(initialData -> checkPlayerRewards(player))
                .exceptionally(throwable -> {
                    this.logger.error("Could not create initial player data: %s".formatted(throwable.getMessage()));
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
                        player.sendMessage(Language.PREFIX.asColoredString() + Language.CLAIM_REWARD_RESET.asColoredString());
                        return;
                    }

                    long delayTime = this.plugin.getConfig().getLong("events.auto-claim-delay");
                    if (this.plugin.getConfig().getBoolean("events.auto-claim-reward")) {
                        this.scheduler.runTaskLater(() -> {
                            this.rewardManager.giveReward(player, currentDay);
                            this.eventCaller.callEvent(new AutoClaimEvent(player, currentDay));
                        }, delayTime * 20L);
                    }

                    if (this.plugin.getConfig().getBoolean("events.open-gui-when-available")) {
                        this.scheduler.runTask(() -> {
                            this.menuManager.openRewardsMenu(player);
                            if (plugin.getConfig().getBoolean("sound.open.enabled")) {
                                this.soundUtil.playSound(player, "open");
                            }
                        });
                    }

                    if (this.plugin.getConfig().getBoolean("events.notify-when-available")) {
                        player.sendMessage(Language.PREFIX.asColoredString() + Language.EVENTS_NOTIFY_WHEN_AVAILABLE.asColoredString());
                        this.eventCaller.callEvent(new PlayerReceiveReminderEvent(player, currentDay));
                    }
                });
            }
        });
    }

    @EventHandler
    public void updateChecker(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (this.plugin.getConfig().getBoolean("check-updates") && player.hasPermission("ndailyrewards.update-notify")) {
            final var current = new ComparableVersion(this.plugin.getDescription().getVersion());

            supplyAsync(this.versionFetcher::fetchNewestVersion).thenApply(Objects::requireNonNull).whenComplete((newest, error) -> {
                if (error != null || newest.compareTo(current) <= 0) {
                    return;
                }

                player.sendMessage(TextUtils.applyColor(Language.PREFIX.asString() + "&aA new update is available: &e" + newest));
                player.sendMessage(TextUtils.applyColor(Language.PREFIX.asString() + "&aDownload here: &e" + versionFetcher.getDownloadUrl()));
            });
        }
    }
}
