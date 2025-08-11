package org.bxteam.ndailyrewards.manager.reward.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.j256.ormlite.table.TableUtils;
import org.bxteam.commons.scheduler.Scheduler;
import org.bxteam.ndailyrewards.database.DatabaseClient;
import org.bxteam.ndailyrewards.database.wrapper.AbstractOrmLiteDatabase;
import org.bxteam.ndailyrewards.manager.reward.PlayerRewardData;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public class RewardRepositoryOrmLite extends AbstractOrmLiteDatabase implements RewardRepository {
    @Inject
    public RewardRepositoryOrmLite(DatabaseClient client, Scheduler scheduler) throws SQLException {
        super(client, scheduler);
        TableUtils.createTableIfNotExists(client.getConnectionSource(), RewardWrapper.class);
    }

    @Override
    public CompletableFuture<PlayerRewardData> getPlayerRewardData(UUID uuid) {
        return this.select(RewardWrapper.class, uuid.toString())
                .thenApply(wrapper -> {
                    if (wrapper == null) {
                        return new PlayerRewardData(System.currentTimeMillis() / 1000L, 0);
                    }
                    return new PlayerRewardData(wrapper.getNextTime(), wrapper.getNextDay());
                });
    }

    @Override
    public CompletableFuture<Void> updatePlayerRewardData(UUID uuid, long nextTime, int nextDay) {
        return this.selectSafe(RewardWrapper.class, uuid.toString())
                .thenCompose(optionalWrapper -> {
                    RewardWrapper wrapper = optionalWrapper.orElse(new RewardWrapper(uuid.toString(), nextTime, nextDay));

                    wrapper.setNextTime(nextTime);
                    wrapper.setNextDay(nextDay);
                    return this.save(RewardWrapper.class, wrapper);
                })
                .thenAccept(result -> {});
    }

    @Override
    public CompletableFuture<Void> resetPlayerRewardData(UUID uuid, long nextTime) {
        return updatePlayerRewardData(uuid, nextTime, 0);
    }

    @Override
    public CompletableFuture<PlayerRewardData> createPlayerData(UUID uuid, long nextTime) {
        RewardWrapper wrapper = new RewardWrapper(uuid.toString(), nextTime, 0);
        return this.saveIfNotExist(RewardWrapper.class, wrapper)
                .thenApply(saved -> new PlayerRewardData(saved.getNextTime(), saved.getNextDay()));
    }
}
