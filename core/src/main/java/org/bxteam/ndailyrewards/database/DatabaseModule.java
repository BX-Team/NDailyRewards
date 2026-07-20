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
        switch (resolveDatabaseType()) {
            case SQLITE -> this.bind(DatabaseClient.class).to(SQLiteClient.class);
            case MARIADB -> this.bind(DatabaseClient.class).to(MariaDBClient.class);
        }

        this.bind(RewardRepository.class).to(RewardRepositoryOrmLite.class);
    }

    private DatabaseType resolveDatabaseType() {
        String raw = this.plugin.getConfig().getString("database.type", "sqlite");
        try {
            return DatabaseType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            this.plugin.getLogger().warning("Invalid database.type '" + raw + "' — falling back to SQLITE. Valid values: sqlite, mariadb.");
            return DatabaseType.SQLITE;
        }
    }
}
