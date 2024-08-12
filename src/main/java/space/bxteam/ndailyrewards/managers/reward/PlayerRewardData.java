package space.bxteam.ndailyrewards.managers.reward;

public class PlayerRewardData {
    private final long next;
    private final int currentDay;

    public PlayerRewardData(long next, int currentDay) {
        this.next = next;
        this.currentDay = currentDay;
    }

    public long getNext() {
        return next;
    }

    public int getCurrentDay() {
        return currentDay;
    }
}
