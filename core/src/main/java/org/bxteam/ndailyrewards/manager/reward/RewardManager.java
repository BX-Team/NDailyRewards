package org.bxteam.ndailyrewards.manager.reward;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.bukkit.entity.Player;
import org.bxteam.ndailyrewards.api.event.PlayerClaimRewardEvent;
import org.bxteam.ndailyrewards.configuration.Messages;
import org.bxteam.ndailyrewards.configuration.PluginConfig;
import org.bxteam.ndailyrewards.event.EventCaller;
import org.bxteam.ndailyrewards.manager.menu.MenuManager;
import org.bxteam.ndailyrewards.manager.reward.database.RewardRepository;
import org.bxteam.ndailyrewards.messaging.MessageService;
import org.bxteam.quark.common.QuarkLogger;
import org.bxteam.quark.paper.PaperScheduler;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class RewardManager {
    private final PluginConfig config;
    private final Messages messages;
    private final RewardRepository rewardRepository;
    private final Provider<MenuManager> menuManagerProvider;
    private final QuarkLogger logger;
    private final EventCaller eventCaller;
    private final ActionsExecutor.Factory actionsExecutorFactory;
    private final PaperScheduler scheduler;
    private final MessageService messageService;

    private final Map<Integer, Reward> rewards = new HashMap<>();

    private final Map<UUID, PlayerRewardData> playerDataCache = new ConcurrentHashMap<>();
    private final Set<UUID> claimsInProgress = ConcurrentHashMap.newKeySet();
    private boolean resetWhenAllClaimed;
    private int cooldown;
    private int resetTime;
    private boolean unlockAfterMidnight;
    private ZoneId zoneId;

    @Inject
    public RewardManager(PluginConfig config, Messages messages, RewardRepository rewardRepository, Provider<MenuManager> menuManagerProvider,
                         QuarkLogger logger, EventCaller eventCaller, ActionsExecutor.Factory actionsExecutorFactory,
                         PaperScheduler scheduler, MessageService messageService) {
        this.config = config;
        this.messages = messages;
        this.rewardRepository = rewardRepository;
        this.menuManagerProvider = menuManagerProvider;
        this.logger = logger;
        this.eventCaller = eventCaller;
        this.actionsExecutorFactory = actionsExecutorFactory;
        this.scheduler = scheduler;
        this.messageService = messageService;
        loadSettings();
        loadRewards();
    }

    public void unload() {
        rewards.clear();
        playerDataCache.clear();
    }

    public void reload() {
        rewards.clear();
        loadSettings();
        loadRewards();
    }

    public PlayerRewardData getCachedPlayerRewardData(UUID uuid) {
        return playerDataCache.get(uuid);
    }

    public void invalidateCache(UUID uuid) {
        playerDataCache.remove(uuid);
    }

    private void loadSettings() {
        PluginConfig.Rewards settings = config.rewards;
        this.resetWhenAllClaimed = settings.resetWhenAllClaimed;
        this.cooldown = settings.cooldown;
        this.resetTime = settings.resetTime;
        this.unlockAfterMidnight = settings.unlockAfterMidnight;
        // validated when the config loads
        this.zoneId = settings.zoneId();
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public int getMaxConfiguredDay() {
        return rewards.size();
    }

    private void loadRewards() {
        config.rewards.days.forEach((day, settings) -> rewards.put(day, new Reward(settings.lore, settings.actions)));
    }

    public void giveReward(Player player, int day) {
        UUID uuid = player.getUniqueId();

        // Clicks held back client-side (e.g. UI Utils' packet delay) arrive in one burst, and without this every
        // one of them reads the old day before the first write lands, so each would be granted (#174).
        if (!claimsInProgress.add(uuid)) {
            logger.debug(() -> "Ignoring claim from " + player.getName() + " for day " + day + ": one is in progress");
            return;
        }

        claimRewardAsync(player, day).whenComplete((ignored, throwable) -> {
            claimsInProgress.remove(uuid);
            if (throwable != null) {
                this.logger.error("Could not claim reward for " + player.getName(), throwable);
            }
        });
    }

    private CompletableFuture<Void> claimRewardAsync(Player player, int day) {
        UUID uuid = player.getUniqueId();
        logger.debug(() -> "Checking if " + player.getName() + " can claim reward for day " + day);

        return getPlayerRewardDataAsync(uuid).thenCompose(data -> {
            Reward reward = rewards.get(day);
            if (reward == null || !isRewardAvailable(data, day)) {
                logger.debug(() -> "Player " + player.getName() + " cannot claim reward for day " + day);
                messageService.send(player, messages.claim.notAvailable);
                return CompletableFuture.completedFuture(null);
            }

            return checkResetForPlayerAsync(uuid).thenCompose(wasReset -> {
                if (wasReset) {
                    logger.debug(() -> "Player " + player.getName() + " reward was reset.");
                    messageService.send(player, messages.claim.rewardReset);
                    return CompletableFuture.completedFuture(null);
                }

                // Saved before the actions run: if the write fails the player gets nothing, rather than a reward
                // they can claim again.
                return updatePlayerRewardDataAsync(uuid, day, data).thenCompose(ignored -> {
                    logger.debug(() -> "Executing actions for " + player.getName() + " day " + day);
                    this.scheduler.sync(player, () -> {
                        this.actionsExecutorFactory.create(player, reward.actions()).execute();
                        this.eventCaller.callEvent(new PlayerClaimRewardEvent(player, day));

                        if (player.getOpenInventory().getTopInventory().getHolder() instanceof MenuManager.MainMenuHolder) {
                            logger.debug(() -> "Refreshing inventory for " + player.getName());
                            this.menuManagerProvider.get().refreshPlayerInventory(player);
                        }
                    });

                    if (resetWhenAllClaimed && day >= rewards.size()) {
                        logger.debug(() -> "Resetting rewards for " + player.getName() + " as all claimed.");
                        return resetPlayerRewardDataAsync(uuid, 0);
                    }
                    return CompletableFuture.completedFuture(null);
                });
            });
        });
    }

    private CompletableFuture<Void> updatePlayerRewardDataAsync(UUID uuid, int nextDay, PlayerRewardData current) {
        long nextTime = getUnixTimeForNextDay(false, false);
        long now = System.currentTimeMillis() / 1000L;
        int newMaxStreak = Math.max(current.maxStreak(), nextDay);
        logger.debug(() -> "Updating reward data async for " + uuid + " nextDay: " + nextDay + " nextTime: " + nextTime);
        return this.rewardRepository.updatePlayerRewardData(uuid, nextTime, nextDay, newMaxStreak, current.missedTotal(), now)
                .thenRun(() -> playerDataCache.put(uuid, new PlayerRewardData(nextTime, nextDay, newMaxStreak, current.missedTotal(), now)));
    }

    public CompletableFuture<PlayerRewardData> createInitialPlayerData(UUID uuid) {
        return rewardRepository.createPlayerData(uuid, getUnixTimeForNextDay(true, false))
                .thenApply(data -> {
                    if (data != null) playerDataCache.put(uuid, data);
                    return data;
                })
                .exceptionally(throwable -> {
                    this.logger.error("Could not create initial player data", throwable);
                    return null;
                });
    }

    private CompletableFuture<Void> resetPlayerRewardDataAsync(UUID uuid, int missedToAdd) {
        long nextTime = getUnixTimeForNextDay(false, true);
        logger.debug(() -> "Resetting reward data async for " + uuid + " missedToAdd: " + missedToAdd + " nextTime: " + nextTime);
        return rewardRepository.resetPlayerRewardData(uuid, nextTime, missedToAdd)
                .thenRun(() -> refreshCache(uuid))
                .exceptionally(throwable -> {
                    this.logger.error("Could not reset player reward data", throwable);
                    return null;
                });
    }

    /** Re-reads a player's data from storage into the in-memory cache. */
    private void refreshCache(UUID uuid) {
        getPlayerRewardDataAsync(uuid);
    }

    public CompletableFuture<PlayerRewardData> getPlayerRewardDataAsync(UUID uuid) {
        return this.rewardRepository.getPlayerRewardData(uuid)
                .thenApply(data -> {
                    if (data != null) playerDataCache.put(uuid, data);
                    return data;
                })
                .exceptionally(throwable -> {
                    this.logger.error("Could not retrieve player reward data", throwable);
                    return null;
                });
    }

    @Deprecated
    public PlayerRewardData getPlayerRewardData(UUID uuid) {
        try {
            return getPlayerRewardDataAsync(uuid).get();
        } catch (Exception e) {
            this.logger.error("Could not retrieve player reward data", e);
            return null;
        }
    }

    public CompletableFuture<Boolean> canClaimRewardAsync(UUID uuid) {
        return getPlayerRewardDataAsync(uuid).thenApply(data -> {
            if (data == null) return false;

            long currentTime = System.currentTimeMillis() / 1000L;
            return currentTime >= data.next() && data.currentDay() < rewards.size();
        });
    }


    public boolean isRewardClaimed(PlayerRewardData playerRewardData, int day) {
        if (playerRewardData == null) return false;

        return playerRewardData.currentDay() >= day;
    }

    public CompletableFuture<Boolean> isRewardAvailableAsync(PlayerRewardData playerRewardData, int day) {
        if (playerRewardData == null) return CompletableFuture.completedFuture(false);

        return CompletableFuture.completedFuture(
            playerRewardData.currentDay() + 1 == day && System.currentTimeMillis() / 1000L >= playerRewardData.next()
        );
    }

    public boolean isRewardAvailable(PlayerRewardData playerRewardData, int day) {
        if (playerRewardData == null) return false;

        return playerRewardData.currentDay() + 1 == day && System.currentTimeMillis() / 1000L >= playerRewardData.next();
    }

    public boolean isRewardNext(PlayerRewardData playerRewardData, int day) {
        if (playerRewardData == null) return false;

        return playerRewardData.currentDay() + 1 == day && System.currentTimeMillis() / 1000L < playerRewardData.next();
    }

    public CompletableFuture<Boolean> checkResetForPlayerAsync(UUID uuid) {
        return getPlayerRewardDataAsync(uuid).thenCompose(data -> {
            if (data == null) return CompletableFuture.completedFuture(false);
            long currentTime = System.currentTimeMillis() / 1000L;
            if (currentTime >= data.next() + resetTime * 3600L) {
                int missedDays = computeMissedDaysSince(data, currentTime);
                return resetPlayerRewardDataAsync(uuid, missedDays).thenApply(v -> true);
            }
            return CompletableFuture.completedFuture(false);
        }).exceptionally(throwable -> {
            this.logger.error("Could not check reset for player", throwable);
            return false;
        });
    }

    public int computeMissedDaysSince(PlayerRewardData data, long nowSeconds) {
        if (data == null) return 0;
        long reference = data.lastClaimTime() > 0 ? data.lastClaimTime() : data.next();
        long diff = nowSeconds - reference;
        if (diff <= 0) return 0;
        int days = (int) (diff / 86400L);
        return Math.max(0, days - 1);
    }

    public boolean hasClaimedToday(PlayerRewardData data) {
        if (data == null || data.lastClaimTime() <= 0) return false;
        LocalDate today = LocalDate.now(zoneId);
        LocalDate claimedDate = Instant.ofEpochSecond(data.lastClaimTime()).atZone(zoneId).toLocalDate();
        return today.equals(claimedDate);
    }

    private long getUnixTimeForNextDay(boolean isFirstJoin, boolean isReset) {
        if (isFirstJoin && config.rewards.firstJoinReward) {
            return Instant.now().getEpochSecond();
        }

        if (isReset && config.rewards.firstDayOnReset) {
            return Instant.now().getEpochSecond();
        }

        if (unlockAfterMidnight) {
            LocalDate tomorrow = LocalDate.now(zoneId).plusDays(1);
            return tomorrow.atStartOfDay(zoneId).toEpochSecond();
        } else {
            return Instant.now().plusSeconds(cooldown * 3600L).getEpochSecond();
        }
    }

    public void handleReward(Player player, int day) {
        logger.debug(() -> "Handling reward click for " + player.getName() + " day: " + day);
        getPlayerRewardDataAsync(player.getUniqueId()).thenAccept(playerRewardData -> {
            if (isRewardClaimed(playerRewardData, day)) {
                messageService.send(player, messages.claim.alreadyClaimed);
            } else if (isRewardAvailable(playerRewardData, day)) {
                giveReward(player, day);
            } else if (isRewardNext(playerRewardData, day)) {
                messageService.send(player, messages.claim.availableSoon);
            } else {
                messageService.send(player, messages.claim.notAvailable);
            }
        });
    }

    public void setDay(Player player, int day) {
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis() / 1000L;
        this.rewardRepository.updatePlayerRewardData(uuid, currentTime, day)
                .thenRun(() -> refreshCache(uuid))
                .exceptionally(throwable -> {
                    this.logger.error("Could not set player day", throwable);
                    return null;
                });
    }
}
