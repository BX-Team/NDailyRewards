package org.bxteam.ndailyrewards.database.function;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLExceptionFunction<T, R> {
    R apply(T t) throws SQLException;
}
