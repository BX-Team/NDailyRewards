package space.bxteam.ndailyrewards.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ConfigReader {
    public @NotNull FileConfiguration config;

    public ConfigReader(@NotNull final FileConfiguration config) {
        this.config = config;
    }

    // language
    public @NotNull String language() {
        return Objects.requireNonNull(config.getString("language"));
    }

    // database.type
    public @NotNull String databaseType() {
        return Objects.requireNonNull(config.getString("database.type"));
    }

    // database.sqlite.file
    public @NotNull String databaseSqliteFile() {
        return Objects.requireNonNull(config.getString("database.sqlite.file"));
    }

    // database.mysql.jdbc
    public @NotNull String databaseMysqlJdbc() {
        return Objects.requireNonNull(config.getString("database.mysql.jdbc"));
    }

    // database.mysql.username
    public @NotNull String databaseMysqlUsername() {
        return Objects.requireNonNull(config.getString("database.mysql.username"));
    }

    // database.mysql.password
    public @NotNull String databaseMysqlPassword() {
        return Objects.requireNonNull(config.getString("database.mysql.password"));
    }
}
