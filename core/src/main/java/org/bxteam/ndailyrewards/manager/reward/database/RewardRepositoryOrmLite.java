package org.bxteam.ndailyrewards.manager.reward.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.j256.ormlite.dao.Dao;
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
    private final Dao<RewardWrapper, String> rewardDao;

    @Inject
    public RewardRepositoryOrmLite(DatabaseClient client, Scheduler scheduler) throws SQLException {
        super(client, scheduler);
        TableUtils.createTableIfNotExists(client.getConnectionSource(), RewardWrapper.class);
        this.rewardDao = client.getDao(RewardWrapper.class);
    }

    @Override
    public CompletableFuture<PlayerRewardData> getPlayerRewardData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                RewardWrapper wrapper = rewardDao.queryForId(uuid.toString());
                if (wrapper == null) {
                    return new PlayerRewardData(System.currentTimeMillis() / 1000L, 0);
                }
                return new PlayerRewardData(wrapper.getNextTime(), wrapper.getNextDay());
            } catch (SQLException e) {
                throw new RuntimeException("Could not retrieve player reward data", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> updatePlayerRewardData(UUID uuid, long nextTime, int nextDay) {
        return CompletableFuture.runAsync(() -> {
            try {
                RewardWrapper wrapper = rewardDao.queryForId(uuid.toString());
                if (wrapper == null) {
                    wrapper = new RewardWrapper(uuid.toString(), nextTime, nextDay);
                    rewardDao.create(wrapper);
                } else {
                    wrapper.setNextTime(nextTime);
                    wrapper.setNextDay(nextDay);
                    rewardDao.update(wrapper);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Could not update player reward data", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> resetPlayerRewardData(UUID uuid, long nextTime) {
        return updatePlayerRewardData(uuid, nextTime, 0);
    }

    @Override
    public CompletableFuture<Void> createPlayerData(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            try {
                RewardWrapper existing = rewardDao.queryForId(uuid.toString());
                if (existing == null) {
                    long currentTime = System.currentTimeMillis() / 1000L;
                    RewardWrapper wrapper = new RewardWrapper(uuid.toString(), currentTime, 0);
                    rewardDao.create(wrapper);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Could not create player data", e);
            }
        });
    }
}
