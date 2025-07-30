package org.bxteam.ndailyrewards.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

public interface DatabaseClient extends Connector {
    ConnectionSource getConnectionSource();

    <T, ID> Dao<T, ID> getDao(Class<T> type);
}
