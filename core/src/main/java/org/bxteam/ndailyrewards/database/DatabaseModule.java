package org.bxteam.ndailyrewards.database;

import com.google.inject.AbstractModule;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.bxteam.helix.database.DatabaseClient;
import org.bxteam.ndailyrewards.database.clients.MariaDBClient;
import org.bxteam.ndailyrewards.database.clients.SQLiteClient;
import org.bxteam.ndailyrewards.manager.reward.database.RewardRepository;
import org.bxteam.ndailyrewards.manager.reward.database.RewardRepositoryOrmLite;

@RequiredArgsConstructor
public class DatabaseModule extends AbstractModule {
    private final Plugin plugin;

    @Override
    protected void configure() {
        switch (DatabaseType.valueOf(this.plugin.getConfig().getString("database.type").toUpperCase())) {
            case SQLITE -> this.bind(DatabaseClient.class).to(SQLiteClient.class);
            case MARIADB -> this.bind(DatabaseClient.class).to(MariaDBClient.class);
        }

        this.bind(RewardRepository.class).to(RewardRepositoryOrmLite.class);
    }
}
