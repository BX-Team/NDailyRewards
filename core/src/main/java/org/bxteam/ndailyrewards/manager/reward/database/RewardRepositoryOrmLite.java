package org.bxteam.ndailyrewards.manager.reward.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableUtils;
import org.bxteam.helix.database.DatabaseClient;
import org.bxteam.helix.database.wrapper.AbstractOrmLiteDatabase;
import org.bxteam.helix.logger.ExtendedLogger;
import org.bxteam.helix.scheduler.Scheduler;
import org.bxteam.ndailyrewards.manager.reward.PlayerRewardData;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public class RewardRepositoryOrmLite extends AbstractOrmLiteDatabase implements RewardRepository {
    private final ExtendedLogger logger;

    @Inject
    public RewardRepositoryOrmLite(DatabaseClient client, Scheduler scheduler, ExtendedLogger logger) throws SQLException {
        super(client, scheduler);
        this.logger = logger;
        TableUtils.createTableIfNotExists(client.getConnectionSource(), RewardWrapper.class);
        runSchemaMigrations(client);
    }

    private void runSchemaMigrations(DatabaseClient client) {
        String[] migrations = {
                "ALTER TABLE data ADD COLUMN max_streak INTEGER DEFAULT 0",
                "ALTER TABLE data ADD COLUMN missed_total INTEGER DEFAULT 0",
                "ALTER TABLE data ADD COLUMN last_claim_time BIGINT DEFAULT 0"
        };

        DatabaseConnection connection = null;
        try {
            connection = client.getConnectionSource().getReadWriteConnection("data");
            for (String sql : migrations) {
                try {
                    connection.executeStatement(sql, DatabaseConnection.DEFAULT_RESULT_FLAGS);
                } catch (SQLException ignored) {
                    // Column probably already exists — safe to ignore
                }
            }
        } catch (SQLException e) {
            logger.warn("Failed to obtain DB connection for migrations: %s".formatted(e.getMessage()));
        } finally {
            if (connection != null) {
                try {
                    client.getConnectionSource().releaseConnection(connection);
                } catch (SQLException ignored) {}
            }
        }
    }

    private PlayerRewardData toData(RewardWrapper wrapper) {
        return new PlayerRewardData(
                wrapper.nextTime(),
                wrapper.nextDay(),
                wrapper.maxStreak(),
                wrapper.missedTotal(),
                wrapper.lastClaimTime()
        );
    }

    @Override
    public CompletableFuture<PlayerRewardData> getPlayerRewardData(UUID uuid) {
        return this.select(RewardWrapper.class, uuid.toString())
                .thenApply(wrapper -> {
                    if (wrapper == null) {
                        return new PlayerRewardData(System.currentTimeMillis() / 1000L, 0, 0, 0, 0L);
                    }
                    return toData(wrapper);
                });
    }

    @Override
    public CompletableFuture<Void> updatePlayerRewardData(UUID uuid, long nextTime, int nextDay) {
        return this.selectSafe(RewardWrapper.class, uuid.toString())
                .thenCompose(optionalWrapper -> {
                    RewardWrapper wrapper = optionalWrapper.orElse(new RewardWrapper(uuid.toString(), nextTime, nextDay));
                    wrapper.nextTime(nextTime);
                    wrapper.nextDay(nextDay);
                    return this.save(RewardWrapper.class, wrapper);
                })
                .thenAccept(result -> {});
    }

    @Override
    public CompletableFuture<Void> updatePlayerRewardData(UUID uuid, long nextTime, int nextDay,
                                                          int maxStreak, int missedTotal, long lastClaimTime) {
        return this.selectSafe(RewardWrapper.class, uuid.toString())
                .thenCompose(optionalWrapper -> {
                    RewardWrapper wrapper = optionalWrapper.orElse(new RewardWrapper(uuid.toString(), nextTime, nextDay));
                    wrapper.nextTime(nextTime);
                    wrapper.nextDay(nextDay);
                    wrapper.maxStreak(maxStreak);
                    wrapper.missedTotal(missedTotal);
                    wrapper.lastClaimTime(lastClaimTime);
                    return this.save(RewardWrapper.class, wrapper);
                })
                .thenAccept(result -> {});
    }

    @Override
    public CompletableFuture<Void> resetPlayerRewardData(UUID uuid, long nextTime) {
        return resetPlayerRewardData(uuid, nextTime, 0);
    }

    @Override
    public CompletableFuture<Void> resetPlayerRewardData(UUID uuid, long nextTime, int missedToAdd) {
        return this.selectSafe(RewardWrapper.class, uuid.toString())
                .thenCompose(optionalWrapper -> {
                    RewardWrapper wrapper = optionalWrapper.orElse(new RewardWrapper(uuid.toString(), nextTime, 0));
                    wrapper.nextTime(nextTime);
                    wrapper.nextDay(0);
                    wrapper.missedTotal(wrapper.missedTotal() + Math.max(0, missedToAdd));
                    return this.save(RewardWrapper.class, wrapper);
                })
                .thenAccept(result -> {});
    }

    @Override
    public CompletableFuture<PlayerRewardData> createPlayerData(UUID uuid, long nextTime) {
        RewardWrapper wrapper = new RewardWrapper(uuid.toString(), nextTime, 0);
        return this.saveIfNotExist(RewardWrapper.class, wrapper)
                .thenApply(this::toData);
    }
}
