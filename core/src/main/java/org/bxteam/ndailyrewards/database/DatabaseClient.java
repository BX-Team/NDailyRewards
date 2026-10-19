package org.bxteam.ndailyrewards.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

public interface DatabaseClient {
    void open() throws SQLException;

    void close();

    boolean available();

    ConnectionSource getConnectionSource();

    <T, ID> Dao<T, ID> getDao(Class<T> type);
}
