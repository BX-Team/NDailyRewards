package org.bxteam.ndailyrewards.manager.reward;

public record PlayerRewardData(
        long next,
        int currentDay,
        int maxStreak,
        int missedTotal,
        long lastClaimTime
) {
    public PlayerRewardData(long next, int currentDay) {
        this(next, currentDay, 0, 0, 0L);
    }
}
