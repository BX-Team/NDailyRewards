package org.bxteam.ndailyrewards.manager.reward.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@DatabaseTable(tableName = "data")
public class RewardWrapper {
    @DatabaseField(id = true, columnName = "uuid")
    private String uuid;

    @DatabaseField(columnName = "next_time")
    private long nextTime;

    @DatabaseField(columnName = "next_day")
    private int nextDay;

    @DatabaseField(columnName = "max_streak", defaultValue = "0")
    private int maxStreak;

    @DatabaseField(columnName = "missed_total", defaultValue = "0")
    private int missedTotal;

    @DatabaseField(columnName = "last_claim_time", defaultValue = "0")
    private long lastClaimTime;

    public RewardWrapper(String uuid, long nextTime, int nextDay) {
        this.uuid = uuid;
        this.nextTime = nextTime;
        this.nextDay = nextDay;
        this.maxStreak = 0;
        this.missedTotal = 0;
        this.lastClaimTime = 0L;
    }
}
