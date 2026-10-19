package org.bxteam.ndailyrewards.database.clients;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.zaxxer.hikari.HikariDataSource;
import org.bxteam.ndailyrewards.database.DatabaseClient;
import org.bxteam.ndailyrewards.configuration.PluginConfig;
import org.bxteam.quark.common.QuarkLogger;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SQLiteClient implements DatabaseClient {
    private final PluginConfig config;
    private final QuarkLogger logger;
    private final Map<Class<?>, Dao<?, ?>> cachedDao = new ConcurrentHashMap<>();
    private final Path dataDirectory;

    private HikariDataSource dataSource;
    private ConnectionSource connectionSource;

    @Inject
    public SQLiteClient(@Named("dataFolder") Path dataDirectory, PluginConfig config, QuarkLogger logger) {
        this.dataDirectory = dataDirectory;
        this.config = config;
        this.logger = logger;
    }

    @Override
    public void open() throws SQLException {
        this.dataSource = new HikariDataSource();
        this.dataSource.addDataSourceProperty("cachePrepStmts", true);
        this.dataSource.addDataSourceProperty("prepStmtCacheSize", 250);
        this.dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        this.dataSource.addDataSourceProperty("useServerPrepStmts", true);
        this.dataSource.setMaximumPoolSize(5);
        this.dataSource.setPoolName("NDailyRewards");
        this.dataSource.setDriverClassName("org.sqlite.JDBC");
        this.dataSource.setJdbcUrl("jdbc:sqlite:" + this.dataDirectory.toAbsolutePath() + "/" + this.config.database.sqlite.file);

        this.connectionSource = new DataSourceConnectionSource(this.dataSource, this.dataSource.getJdbcUrl());
    }

    @Override
    public void close() {
        try {
            this.dataSource.close();
            this.connectionSource.close();
        } catch (Exception e) {
            this.logger.error("Could not close the SQLite connection pool", e);
        }
    }

    @Override
    public boolean available() {
        try (var connection = this.dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public ConnectionSource getConnectionSource() {
        return this.connectionSource;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, ID> Dao<T, ID> getDao(Class<T> type) {
        try {
            Dao<?, ?> dao = this.cachedDao.get(type);

            if (dao == null) {
                dao = DaoManager.createDao(this.connectionSource, type);
                this.cachedDao.put(type, dao);
            }

            return (Dao<T, ID>) dao;
        }
        catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
