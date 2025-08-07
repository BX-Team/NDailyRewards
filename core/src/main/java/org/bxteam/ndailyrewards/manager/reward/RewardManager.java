package org.bxteam.ndailyrewards.manager.reward;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bxteam.commons.logger.ExtendedLogger;
import org.bxteam.commons.scheduler.Scheduler;
import org.bxteam.ndailyrewards.api.event.PlayerClaimRewardEvent;
import org.bxteam.ndailyrewards.event.EventCaller;
import org.bxteam.ndailyrewards.manager.menu.MenuManager;
import org.bxteam.ndailyrewards.manager.reward.database.RewardRepository;
import org.bxteam.ndailyrewards.configuration.Language;

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

    private final Map<Integer, Reward> rewards = new HashMap<>();
    private final boolean resetWhenAllClaimed;
    private final int cooldown;
    private final int resetTime;
    private final boolean unlockAfterMidnight;

    @Inject
    public RewardManager(Plugin plugin, RewardRepository rewardRepository, Provider<MenuManager> menuManagerProvider,
                        ExtendedLogger logger, EventCaller eventCaller, ActionsExecutor.Factory actionsExecutorFactory, Scheduler scheduler) {
        this.plugin = plugin;
        this.rewardRepository = rewardRepository;
        this.menuManagerProvider = menuManagerProvider;
        this.logger = logger;
        this.eventCaller = eventCaller;
        this.actionsExecutorFactory = actionsExecutorFactory;
        this.scheduler = scheduler;
        this.resetWhenAllClaimed = plugin.getConfig().getBoolean("rewards.reset-when-all-claimed", true);
        this.cooldown = plugin.getConfig().getInt("rewards.cooldown", 24);
        this.resetTime = plugin.getConfig().getInt("rewards.reset-time", 24);
        this.unlockAfterMidnight = plugin.getConfig().getBoolean("rewards.unlock-after-midnight", false);
        loadRewards();
    }

    public void unload() {
        rewards.clear();
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

        canClaimRewardAsync(uuid).thenAccept(canClaim -> {
            if (!canClaim) {
                player.sendMessage(Language.PREFIX.asColoredString() + Language.CLAIM_NOT_AVAILABLE.asColoredString());
                return;
            }

            checkResetForPlayerAsync(uuid).thenAccept(wasReset -> {
                if (wasReset) {
                    player.sendMessage(Language.PREFIX.asColoredString() + Language.CLAIM_REWARD_RESET.asColoredString());
                    return;
                }

                Reward reward = rewards.get(day);
                if (reward != null) {
                    this.scheduler.runTask(() -> {
                        ActionsExecutor executor = actionsExecutorFactory.create(player, reward);
                        executor.execute();
                    });

                    updatePlayerRewardDataAsync(uuid, day).thenRun(() -> {
                        this.scheduler.runTask(() -> {
                            if (player.getOpenInventory().getTopInventory().getHolder() instanceof MenuManager.MainMenuHolder) {
                                menuManagerProvider.get().refreshPlayerInventory(player);
                            }
                        });
                    });

                    if (resetWhenAllClaimed && day >= rewards.size()) {
                        resetPlayerRewardDataAsync(uuid);
                    }

                    eventCaller.callEvent(new PlayerClaimRewardEvent(player, day));
                }
            });
        });
    }

    private CompletableFuture<Void> updatePlayerRewardDataAsync(UUID uuid, int nextDay) {
        long nextTime = getUnixTimeForNextDay();
        return rewardRepository.updatePlayerRewardData(uuid, nextTime, nextDay)
                .exceptionally(throwable -> {
                    this.logger.error("Could not update player reward data: %s".formatted(throwable.getMessage()));
                    throwable.printStackTrace();
                    return null;
                });
    }

    private CompletableFuture<Void> resetPlayerRewardDataAsync(UUID uuid) {
        long nextTime = getUnixTimeForNextDay();
        return rewardRepository.resetPlayerRewardData(uuid, nextTime)
                .exceptionally(throwable -> {
                    this.logger.error("Could not reset player reward data: %s".formatted(throwable.getMessage()));
                    throwable.printStackTrace();
                    return null;
                });
    }

    public CompletableFuture<PlayerRewardData> getPlayerRewardDataAsync(UUID uuid) {
        return rewardRepository.getPlayerRewardData(uuid)
                .exceptionally(throwable -> {
                    this.logger.error("Could not retrieve player reward data: %s".formatted(throwable.getMessage()));
                    throwable.printStackTrace();
                    return new PlayerRewardData(System.currentTimeMillis() / 1000L, 0);
                });
    }

    public PlayerRewardData getPlayerRewardData(UUID uuid) {
        try {
            return getPlayerRewardDataAsync(uuid).get();
        } catch (Exception e) {
            this.logger.error("Could not retrieve player reward data: %s".formatted(e.getMessage()));
            return new PlayerRewardData(System.currentTimeMillis() / 1000L, 0);
        }
    }

    public CompletableFuture<Boolean> canClaimRewardAsync(UUID uuid) {
        return getPlayerRewardDataAsync(uuid).thenApply(data -> {
            long currentTime = System.currentTimeMillis() / 1000L;
            return currentTime >= data.next() && data.currentDay() < rewards.size();
        });
    }

    private long getUnixTimeForNextDay() {
        if (unlockAfterMidnight) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            return tomorrow.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        } else {
            return Instant.now().plusSeconds(cooldown * 3600L).getEpochSecond();
        }
    }

    public boolean shouldResetWhenAllClaimed() {
        return resetWhenAllClaimed;
    }

    public boolean isRewardClaimed(PlayerRewardData playerRewardData, int day) {
        return playerRewardData.currentDay() >= day;
    }

    public boolean isRewardAvailable(PlayerRewardData playerRewardData, int day) {
        return playerRewardData.currentDay() + 1 == day && System.currentTimeMillis() / 1000L >= playerRewardData.next();
    }

    public boolean isRewardNext(PlayerRewardData playerRewardData, int day) {
        return playerRewardData.currentDay() + 1 == day && System.currentTimeMillis() / 1000L < playerRewardData.next();
    }

    public CompletableFuture<Boolean> checkResetForPlayerAsync(UUID uuid) {
        return getPlayerRewardDataAsync(uuid).thenCompose(data -> {
            long currentTime = System.currentTimeMillis() / 1000L;
            if (currentTime >= data.next() + resetTime * 3600L) {
                return resetPlayerRewardDataAsync(uuid).thenApply(v -> true);
            }
            return CompletableFuture.completedFuture(false);
        });
    }

    public boolean checkResetForPlayer(UUID uuid) {
        try {
            return checkResetForPlayerAsync(uuid).get();
        } catch (Exception e) {
            this.logger.error("Could not check reset for player: %s".formatted(e.getMessage()));
            return false;
        }
    }

    public void handleReward(Player player, int day) {
        getPlayerRewardDataAsync(player.getUniqueId()).thenAccept(playerRewardData -> {
            if (isRewardClaimed(playerRewardData, day)) {
                player.sendMessage(Language.PREFIX.asColoredString() + Language.CLAIM_ALREADY_CLAIMED.asColoredString());
            } else if (isRewardAvailable(playerRewardData, day)) {
                giveReward(player, day);
                // Remove the immediate menu opening - it will be refreshed after database update
            } else if (isRewardNext(playerRewardData, day)) {
                player.sendMessage(Language.PREFIX.asColoredString() + Language.CLAIM_AVAILABLE_SOON.asColoredString());
            } else {
                player.sendMessage(Language.PREFIX.asColoredString() + Language.CLAIM_NOT_AVAILABLE.asColoredString());
            }
        });
    }

    public void setDay(Player player, int day) {
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis() / 1000L;
        rewardRepository.updatePlayerRewardData(uuid, currentTime, day)
                .exceptionally(throwable -> {
                    this.logger.error("Could not set player day: %s".formatted(throwable.getMessage()));
                    throwable.printStackTrace();
                    return null;
                });
    }
}
