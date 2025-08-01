package org.bxteam.ndailyrewards.listener;

import com.google.inject.Inject;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bxteam.commons.logger.ExtendedLogger;
import org.bxteam.commons.scheduler.Scheduler;
import org.bxteam.commons.updater.VersionFetcher;
import org.bxteam.ndailyrewards.event.EventCaller;
import org.bxteam.ndailyrewards.manager.menu.MenuManager;
import org.bxteam.ndailyrewards.manager.reward.database.RewardRepository;
import org.bxteam.ndailyrewards.utils.Permissions;
import org.bxteam.ndailyrewards.utils.SoundUtil;
import org.bxteam.ndailyrewards.utils.TextUtils;
import org.bxteam.ndailyrewards.api.event.AutoClaimEvent;
import org.bxteam.ndailyrewards.api.event.PlayerReceiveReminderEvent;
import org.bxteam.ndailyrewards.configuration.Language;
import org.bxteam.ndailyrewards.manager.reward.RewardManager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class PlayerJoinListener implements Listener {
    private final Plugin plugin;
    private final RewardManager rewardManager;
    private final MenuManager menuManager;
    private final RewardRepository rewardRepository;
    private final ExtendedLogger logger;
    private final VersionFetcher versionFetcher;
    private final Scheduler scheduler;
    private final EventCaller eventCaller;
    private final SoundUtil soundUtil;

    @Inject
    public PlayerJoinListener(Plugin plugin, RewardManager rewardManager, MenuManager menuManager,
                             RewardRepository rewardRepository, ExtendedLogger logger,
                             VersionFetcher versionFetcher, Scheduler scheduler, EventCaller eventCaller, SoundUtil soundUtil) {
        this.plugin = plugin;
        this.rewardManager = rewardManager;
        this.menuManager = menuManager;
        this.rewardRepository = rewardRepository;
        this.logger = logger;
        this.versionFetcher = versionFetcher;
        this.scheduler = scheduler;
        this.eventCaller = eventCaller;
        this.soundUtil = soundUtil;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        rewardRepository.getPlayerRewardData(uuid).thenAccept(playerData -> {
            if (playerData.currentDay() == 0 && playerData.next() == 0) {
                boolean isFirstJoin = true;
                long nextTime = getUnixTimeForNextDay(isFirstJoin);
                rewardRepository.updatePlayerRewardData(uuid, nextTime, 0)
                        .exceptionally(throwable -> {
                            logger.error("Could not create initial player data: %s".formatted(throwable.getMessage()));
                            return null;
                        });
            }
        }).exceptionally(throwable -> {
            logger.error("Could not check player data: %s".formatted(throwable.getMessage()));
            return null;
        });
    }

    private long getUnixTimeForNextDay(boolean isFirstJoin) {
        if (isFirstJoin && plugin.getConfig().getBoolean("rewards.first-join-reward")) {
            return Instant.now().getEpochSecond();
        }

        if (plugin.getConfig().getBoolean("rewards.unlock-after-midnight")) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            return tomorrow.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        } else {
            return Instant.now().plusSeconds(plugin.getConfig().getInt("rewards.cooldown") * 3600L).getEpochSecond();
        }
    }

    @EventHandler
    public void onPlayerJoinEvents(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        rewardManager.getPlayerRewardDataAsync(player.getUniqueId()).thenAccept(playerRewardData -> {
            int currentDay = playerRewardData.currentDay() + 1;
            long delayTime = plugin.getConfig().getLong("events.auto-claim-delay");

            if (plugin.getConfig().getBoolean("events.auto-claim-reward") &&
                rewardManager.isRewardAvailable(playerRewardData, currentDay)) {

                this.scheduler.runTaskLater(() -> {
                    rewardManager.giveReward(player, currentDay);
                    this.eventCaller.callEvent(new AutoClaimEvent(player, currentDay));
                }, delayTime * 20L);
            }

            if (plugin.getConfig().getBoolean("events.open-gui-when-available") &&
                rewardManager.isRewardAvailable(playerRewardData, currentDay)) {

                rewardManager.checkResetForPlayerAsync(player.getUniqueId()).thenAccept(wasReset -> {
                    if (wasReset) {
                        player.sendMessage(Language.PREFIX.asColoredString() + Language.CLAIM_REWARD_RESET.asColoredString());
                        return;
                    }

                    this.scheduler.runTask(() -> {
                        menuManager.openRewardsMenu(player);
                        if (plugin.getConfig().getBoolean("sound.open.enabled")) {
                            soundUtil.playSound(player, "open");
                        }
                    });
                });
            }

            if (plugin.getConfig().getBoolean("events.notify-when-available") &&
                rewardManager.isRewardAvailable(playerRewardData, currentDay)) {

                rewardManager.checkResetForPlayerAsync(player.getUniqueId()).thenAccept(wasReset -> {
                    if (wasReset) {
                        player.sendMessage(Language.PREFIX.asColoredString() + Language.CLAIM_REWARD_RESET.asColoredString());
                        return;
                    }

                    player.sendMessage(Language.PREFIX.asColoredString() + Language.EVENTS_NOTIFY_WHEN_AVAILABLE.asColoredString());
                    this.eventCaller.callEvent(new PlayerReceiveReminderEvent(player, currentDay));
                });
            }
        });
    }

    @EventHandler
    public void updateChecker(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfig().getBoolean("check-updates") && player.hasPermission(Permissions.UPDATE_NOTIFY)) {
            final var current = new ComparableVersion(plugin.getDescription().getVersion());

            supplyAsync(versionFetcher::fetchNewestVersion).thenApply(Objects::requireNonNull).whenComplete((newest, error) -> {
                if (error != null || newest.compareTo(current) <= 0) {
                    return;
                }

                player.sendMessage(TextUtils.applyColor(Language.PREFIX.asString() + "&aA new update is available: &e" + newest));
                player.sendMessage(TextUtils.applyColor(Language.PREFIX.asString() + "&aDownload here: &e" + versionFetcher.getDownloadUrl()));
            });
        }
    }
}
