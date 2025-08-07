package org.bxteam.ndailyrewards.manager.reward.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "data")
public class RewardWrapper {
    @DatabaseField(id = true, columnName = "uuid")
    private String uuid;

    @DatabaseField(columnName = "next_time")
    private long nextTime;

    @DatabaseField(columnName = "next_day")
    private int nextDay;

    RewardWrapper() { }

    public RewardWrapper(String uuid, long nextTime, int nextDay) {
        this.uuid = uuid;
        this.nextTime = nextTime;
        this.nextDay = nextDay;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getNextTime() {
        return nextTime;
    }

    public void setNextTime(long nextTime) {
        this.nextTime = nextTime;
    }

    public int getNextDay() {
        return nextDay;
    }

    public void setNextDay(int nextDay) {
        this.nextDay = nextDay;
    }
}
