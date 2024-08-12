package space.bxteam.ndailyrewards.managers.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.utils.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;

public class DatabaseManager {
    public HikariConfig hikariConfig = new HikariConfig();
    public HikariDataSource dbSource;

    public DatabaseManager(@NotNull JavaPlugin plugin) {
        setupDatabaseSource();
        try {
            initTables();
        }
        catch (@NotNull SQLException | @NotNull IOException e) {
            LogUtil.log("An error occurred while initializing the database!", LogUtil.LogLevel.ERROR);
            e.printStackTrace();
            NDailyRewards.getInstance().getServer().getPluginManager().disablePlugin(NDailyRewards.getInstance());
        }
    }

    /**
     * Set up the database source
     */
    private void setupDatabaseSource() {
        switch (Objects.requireNonNull(NDailyRewards.getInstance().getConfig().getString("database.type"))) {
            case "sqlite" -> {
                hikariConfig.setDriverClassName("org.sqlite.JDBC");
                hikariConfig.setJdbcUrl("jdbc:sqlite:" + NDailyRewards.getInstance().getDataFolder() + File.separator + NDailyRewards.getInstance().getConfig().getString("database.sqlite.file"));
            }
            case "mysql" -> {
                hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
                hikariConfig.setJdbcUrl(NDailyRewards.getInstance().getConfig().getString("database.mysql.jdbc"));
                hikariConfig.setUsername(NDailyRewards.getInstance().getConfig().getString("database.mysql.username"));
                hikariConfig.setPassword(NDailyRewards.getInstance().getConfig().getString("database.mysql.password"));
            }
            default -> {
                LogUtil.log("Invalid database type! Please check your config.yml", LogUtil.LogLevel.ERROR);
                NDailyRewards.getInstance().getServer().getPluginManager().disablePlugin(NDailyRewards.getInstance());
                return;
            }
        }

        dbSource = new HikariDataSource(hikariConfig);
    }

    /**
     * Initialize the database tables
     *
     * @throws @NotNull SQLException
     * @throws @NotNull IOException
     */
    private void initTables() throws @NotNull SQLException, @NotNull IOException {
        final @NotNull HashMap<@NotNull String, @NotNull String> initFiles = new HashMap<>() {{
            put("sqlite", "databases/sqlite.sql");
            put("mysql", "databases/mysql.sql");
        }};
        final @NotNull String dbType = Objects.requireNonNull(NDailyRewards.getInstance().getConfig().getString("database.type"));
        if (!initFiles.containsKey(dbType)) {
            LogUtil.log("Invalid database type! Please check your config.yml", LogUtil.LogLevel.ERROR);
            NDailyRewards.getInstance().getServer().getPluginManager().disablePlugin(NDailyRewards.getInstance());
            return;
        }
        @NotNull String setupFile = initFiles.get(dbType);
        @NotNull String query;
        try (@NotNull InputStream stream = Objects.requireNonNull(NDailyRewards.getInstance().getResource(setupFile))) {
            query = new @NotNull String(stream.readAllBytes());
        } catch (@NotNull IOException e) {
            LogUtil.log("An error occurred while reading the database setup file!", LogUtil.LogLevel.ERROR);
            throw e;
        }
        final @NotNull String[] queries = query.split(";");
        for (@NotNull String query1 : queries) {
            query1 = query1.stripTrailing().stripIndent().replaceAll("^\\s+(?:--.+)*", "");
            if (query1.isBlank()) continue;
            try (final @NotNull Connection conn = dbSource.getConnection();
                 final @NotNull PreparedStatement stmt = conn.prepareStatement(query1)) {
                stmt.execute();
            }
        }
        LogUtil.log("Database initialized", LogUtil.LogLevel.INFO);
    }
}
