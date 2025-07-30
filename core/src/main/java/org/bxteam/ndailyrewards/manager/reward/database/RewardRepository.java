package org.bxteam.ndailyrewards.manager.reward.database;

import org.bxteam.ndailyrewards.manager.reward.PlayerRewardData;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface RewardRepository {
    CompletableFuture<PlayerRewardData> getPlayerRewardData(UUID uuid);
    CompletableFuture<Void> updatePlayerRewardData(UUID uuid, long nextTime, int nextDay);
    CompletableFuture<Void> resetPlayerRewardData(UUID uuid, long nextTime);
    CompletableFuture<Void> createPlayerData(UUID uuid);
}
