package org.bxteam.ndailyrewards.database;

import com.google.inject.AbstractModule;
import lombok.RequiredArgsConstructor;
import org.bxteam.ndailyrewards.configuration.PluginConfig;
import org.bxteam.ndailyrewards.database.clients.MariaDBClient;
import org.bxteam.ndailyrewards.database.clients.SQLiteClient;
import org.bxteam.ndailyrewards.manager.reward.database.RewardRepository;
import org.bxteam.ndailyrewards.manager.reward.database.RewardRepositoryOrmLite;

@RequiredArgsConstructor
public class DatabaseModule extends AbstractModule {
    private final PluginConfig config;

    @Override
    protected void configure() {
        switch (this.config.database.type) {
            case SQLITE -> this.bind(DatabaseClient.class).to(SQLiteClient.class);
            case MARIADB -> this.bind(DatabaseClient.class).to(MariaDBClient.class);
        }

        this.bind(RewardRepository.class).to(RewardRepositoryOrmLite.class);
    }
}
