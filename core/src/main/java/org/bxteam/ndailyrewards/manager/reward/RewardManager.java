package org.bxteam.ndailyrewards.manager.reward;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bxteam.helix.logger.ExtendedLogger;
import org.bxteam.helix.scheduler.Scheduler;
import org.bxteam.ndailyrewards.api.event.PlayerClaimRewardEvent;
import org.bxteam.ndailyrewards.event.EventCaller;
import org.bxteam.ndailyrewards.manager.menu.MenuManager;
import org.bxteam.ndailyrewards.manager.reward.database.RewardRepository;
import org.bxteam.ndailyrewards.configuration.Language;
import org.bxteam.ndailyrewards.messaging.MessageService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public class RewardManager {
    private final Plugin plugin;
    private final RewardRepository rewardRepository;
    private final Provider<MenuManager> menuManagerProvider;
    private final ExtendedLogger logger;
    private final EventCaller eventCaller;
    private final ActionsExecutor.Factory actionsExecutorFactory;
    private final Scheduler scheduler;
    private final MessageService messageService;

    private final Map<Integer, Reward> rewards = new HashMap<>();
    private boolean resetWhenAllClaimed;
    private int cooldown;
    private int resetTime;
    private boolean unlockAfterMidnight;
    private ZoneId zoneId;
    private boolean debug;

    @Inject
    public RewardManager(Plugin plugin, RewardRepository rewardRepository, Provider<MenuManager> menuManagerProvider,
                        ExtendedLogger logger, EventCaller eventCaller, ActionsExecutor.Factory actionsExecutorFactory,
                        Scheduler scheduler, MessageService messageService) {
        this.plugin = plugin;
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
    }

    public void reload() {
        rewards.clear();
        loadSettings();
        loadRewards();
    }

    private void loadSettings() {
        this.debug = plugin.getConfig().getBoolean("debug", false);
        this.resetWhenAllClaimed = plugin.getConfig().getBoolean("rewards.reset-when-all-claimed", true);
        this.cooldown = plugin.getConfig().getInt("rewards.cooldown", 24);
        this.resetTime = plugin.getConfig().getInt("rewards.reset-time", 24);
        this.unlockAfterMidnight = plugin.getConfig().getBoolean("rewards.unlock-after-midnight", false);
        this.zoneId = resolveZoneId(plugin.getConfig().getString("rewards.timezone", "system"));
    }

    private ZoneId resolveZoneId(String raw) {
        if (raw == null || raw.isBlank() || raw.equalsIgnoreCase("system") || raw.equalsIgnoreCase("default")) {
            return ZoneId.systemDefault();
        }
        try {
            return ZoneId.of(raw);
        } catch (Exception e) {
            logger.warn("Invalid timezone '%s' in config — falling back to system default. Valid examples: UTC, Europe/Moscow, America/New_York".formatted(raw));
            return ZoneId.systemDefault();
        }
    }

    public boolean isDebugEnabled() {
        return debug;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public int getMaxConfiguredDay() {
        return rewards.size();
    }

    private void loadRewards() {
        ConfigurationSection daysSection = plugin.getConfig().getConfigurationSection("rewards.days");
        if (daysSection != null) {
            for (String dayKey : daysSection.getKeys(false)) {
                int day = Integer.parseInt(dayKey);
                ConfigurationSection daySection = daysSection.getConfigurationSection(dayKey);
                if (daySection != null) {
                    List<String> lore = daySection.getStringList("lore");
                    List<String> rewardsList = daySection.getStringList("actions");
                    rewards.put(day, new Reward(lore, rewardsList));
                }
            }
        }
    }

    public void giveReward(Player player, int day) {
        UUID uuid = player.getUniqueId();
        
        if (debug) logger.info("[DEBUG] Checking if " + player.getName() + " can claim reward for day " + day);

        canClaimRewardAsync(uuid).thenAccept(canClaim -> {
            if (!canClaim) {
                if (debug) logger.info("[DEBUG] Player " + player.getName() + " cannot claim reward for day " + day);
                messageService.send(player, Language.CLAIM_NOT_AVAILABLE);
                return;
            }

            checkResetForPlayerAsync(uuid).thenAccept(wasReset -> {
                if (wasReset) {
                    if (debug) logger.info("[DEBUG] Player " + player.getName() + " reward was reset.");
                    messageService.send(player, Language.CLAIM_REWARD_RESET);
                    return;
                }

                Reward reward = rewards.get(day);
                if (reward != null) {
                    if (debug) logger.info("[DEBUG] Executing actions for " + player.getName() + " day " + day);
                    this.scheduler.runTask(() -> {
                        ActionsExecutor executor = this.actionsExecutorFactory.create(player, reward);
                        executor.execute();
                    });

                    updatePlayerRewardDataAsync(uuid, day).thenRun(() -> {
                        this.scheduler.runTask(() -> {
                            if (player.getOpenInventory().getTopInventory().getHolder() instanceof MenuManager.MainMenuHolder) {
                                if (debug) logger.info("[DEBUG] Refreshing inventory for " + player.getName());
                                this.menuManagerProvider.get().refreshPlayerInventory(player);
                            }
                        });
                    });

                    if (resetWhenAllClaimed && day >= rewards.size()) {
                        if (debug) logger.info("[DEBUG] Resetting rewards for " + player.getName() + " as all claimed.");
                        resetPlayerRewardDataAsync(uuid, 0);
                    }

                    this.eventCaller.callEvent(new PlayerClaimRewardEvent(player, day));
                }
            });
        });
    }

    private CompletableFuture<Void> updatePlayerRewardDataAsync(UUID uuid, int nextDay) {
        long nextTime = getUnixTimeForNextDay(false, false);
        long now = System.currentTimeMillis() / 1000L;
        if (debug) logger.info("[DEBUG] Updating reward data async for " + uuid + " nextDay: " + nextDay + " nextTime: " + nextTime);
        return this.rewardRepository.getPlayerRewardData(uuid)
                .thenCompose(current -> {
                    int newMaxStreak = Math.max(current.maxStreak(), nextDay);
                    return this.rewardRepository.updatePlayerRewardData(
                            uuid, nextTime, nextDay, newMaxStreak, current.missedTotal(), now
                    );
                })
                .exceptionally(throwable -> {
                    this.logger.error("Could not update player reward data: %s".formatted(throwable.getMessage()));
                    throwable.printStackTrace();
                    return null;
                });
    }

    public CompletableFuture<PlayerRewardData> createInitialPlayerData(UUID uuid) {
        return rewardRepository.createPlayerData(uuid, getUnixTimeForNextDay(true, false))
                .exceptionally(throwable -> {
                    this.logger.error("Could not create initial player data: %s".formatted(throwable.getMessage()));
                    return null;
                });
    }

    private CompletableFuture<Void> resetPlayerRewardDataAsync(UUID uuid, int missedToAdd) {
        long nextTime = getUnixTimeForNextDay(false, true);
        if (debug) logger.info("[DEBUG] Resetting reward data async for " + uuid + " missedToAdd: " + missedToAdd + " nextTime: " + nextTime);
        return rewardRepository.resetPlayerRewardData(uuid, nextTime, missedToAdd)
                .exceptionally(throwable -> {
                    this.logger.error("Could not reset player reward data: %s".formatted(throwable.getMessage()));
                    throwable.printStackTrace();
                    return null;
                });
    }

    public CompletableFuture<PlayerRewardData> getPlayerRewardDataAsync(UUID uuid) {
        return this.rewardRepository.getPlayerRewardData(uuid)
                .exceptionally(throwable -> {
                    this.logger.error("Could not retrieve player reward data: %s".formatted(throwable.getMessage()));
                    return null;
                });
    }

    public PlayerRewardData getPlayerRewardData(UUID uuid) {
        try {
            return getPlayerRewardDataAsync(uuid).get();
        } catch (Exception e) {
            this.logger.error("Could not retrieve player reward data: %s".formatted(e.getMessage()));
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
            this.logger.error("Could not check reset for player: %s".formatted(throwable.getMessage()));
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
        if (isFirstJoin && this.plugin.getConfig().getBoolean("rewards.first-join-reward")) {
            return Instant.now().getEpochSecond();
        }

        if (isReset && this.plugin.getConfig().getBoolean("rewards.first-day-on-reset")) {
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
        if (debug) logger.info("[DEBUG] Handling reward click for " + player.getName() + " day: " + day);
        getPlayerRewardDataAsync(player.getUniqueId()).thenAccept(playerRewardData -> {
            if (isRewardClaimed(playerRewardData, day)) {
                messageService.send(player, Language.CLAIM_ALREADY_CLAIMED);
            } else if (isRewardAvailable(playerRewardData, day)) {
                giveReward(player, day);
            } else if (isRewardNext(playerRewardData, day)) {
                messageService.send(player, Language.CLAIM_AVAILABLE_SOON);
            } else {
                messageService.send(player, Language.CLAIM_NOT_AVAILABLE);
            }
        });
    }

    public void setDay(Player player, int day) {
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis() / 1000L;
        this.rewardRepository.updatePlayerRewardData(uuid, currentTime, day)
                .exceptionally(throwable -> {
                    this.logger.error("Could not set player day: %s".formatted(throwable.getMessage()));
                    throwable.printStackTrace();
                    return null;
                });
    }
}
