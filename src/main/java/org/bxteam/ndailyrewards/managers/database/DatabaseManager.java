package org.bxteam.ndailyrewards.managers.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.utils.LogUtil;

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

    public DatabaseManager() {
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
            case "mariadb" -> {
                hikariConfig.setDriverClassName(org.mariadb.jdbc.Driver.class.getName());
                hikariConfig.setJdbcUrl(NDailyRewards.getInstance().getConfig().getString("database.mariadb.jdbc"));
                hikariConfig.setUsername(NDailyRewards.getInstance().getConfig().getString("database.mariadb.username"));
                hikariConfig.setPassword(NDailyRewards.getInstance().getConfig().getString("database.mariadb.password"));
            }
            default -> {
                LogUtil.log("Invalid database type! Please check your config.yml", LogUtil.LogLevel.ERROR);
                NDailyRewards.getInstance().getServer().getPluginManager().disablePlugin(NDailyRewards.getInstance());
                return;
            }
        }

        hikariConfig.addDataSourceProperty("cachePrepStmts", NDailyRewards.getInstance().getConfig().getBoolean("database.cachePrepStmts"));
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", NDailyRewards.getInstance().getConfig().getInt("database.prepStmtCacheSize"));
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", NDailyRewards.getInstance().getConfig().getInt("database.prepStmtCacheSqlLimit"));
        hikariConfig.addDataSourceProperty("useServerPrepStmts", NDailyRewards.getInstance().getConfig().getBoolean("database.useServerPrepStmts"));
        hikariConfig.addDataSourceProperty("useLocalSessionState", NDailyRewards.getInstance().getConfig().getBoolean("database.useLocalSessionState"));
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", NDailyRewards.getInstance().getConfig().getBoolean("database.rewriteBatchedStatements"));
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", NDailyRewards.getInstance().getConfig().getBoolean("database.cacheResultSetMetadata"));
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", NDailyRewards.getInstance().getConfig().getBoolean("database.cacheServerConfiguration"));
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", NDailyRewards.getInstance().getConfig().getBoolean("database.elideSetAutoCommits"));
        hikariConfig.addDataSourceProperty("maintainTimeStats", NDailyRewards.getInstance().getConfig().getBoolean("database.maintainTimeStats"));

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
            put("mariadb", "databases/mariadb.sql");
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
