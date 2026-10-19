package org.bxteam.ndailyrewards.database.clients;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.bxteam.ndailyrewards.database.DatabaseClient;
import org.bxteam.ndailyrewards.configuration.PluginConfig;
import org.bxteam.quark.common.QuarkLogger;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MariaDBClient implements DatabaseClient {
    private final PluginConfig config;
    private final QuarkLogger logger;
    private final Map<Class<?>, Dao<?, ?>> cachedDao = new ConcurrentHashMap<>();

    private HikariDataSource dataSource;
    private ConnectionSource connectionSource;

    @Override
    public void open() throws SQLException {
        PluginConfig.Database database = this.config.database;
        this.dataSource = new HikariDataSource();
        database.dataSourceProperties.forEach(this.dataSource::addDataSourceProperty);
        this.dataSource.setMaximumPoolSize(5);
        this.dataSource.setPoolName("NDailyRewards");
        this.dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
        this.dataSource.setJdbcUrl(database.mariadb.jdbc);
        this.dataSource.setUsername(database.mariadb.username);
        this.dataSource.setPassword(database.mariadb.password);

        this.connectionSource = new DataSourceConnectionSource(this.dataSource, this.dataSource.getJdbcUrl());
    }

    @Override
    public void close() {
        try {
            this.dataSource.close();
            this.connectionSource.close();
        } catch (Exception e) {
            this.logger.error("Could not close the MariaDB connection pool", e);
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
