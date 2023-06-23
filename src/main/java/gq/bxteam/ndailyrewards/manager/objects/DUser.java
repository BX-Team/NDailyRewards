package gq.bxteam.ndailyrewards.manager.objects;

import java.util.Calendar;

import gq.bxteam.ndailyrewards.cfg.Config;
import org.bukkit.entity.Player;

public class DUser {
    private final String uuid;
    private final String name;
    private long login;
    private int day_row;
    private long next_reward;
    private long expire_reward;

    public DUser(final Player p) {
        this.uuid = p.getUniqueId().toString();
        this.name = p.getName();
        this.login = System.currentTimeMillis();
        this.resetRewards(false);
    }

    public DUser(final String uuid, final String name, final long login, final int day_row, final long next_reward, final long expire_reward) {
        this.uuid = uuid;
        this.name = name;
        this.login = login;
        this.day_row = day_row;
        this.next_reward = next_reward;
        this.expire_reward = expire_reward;
    }

    public String getUUID() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public long getLastLogin() {
        return this.login;
    }

    public void setLastLogin(final long login) {
        this.login = login;
    }

    public int getDayInRow() {
        return this.day_row;
    }

    public long getNextRewardTime() {
        return this.next_reward;
    }

    public long getTimeToGetReward() {
        return this.expire_reward;
    }

    public void updateNextTime() {
        this.next_reward = System.currentTimeMillis() + this.getNextTime(System.currentTimeMillis());
    }

    public void resetRewards(final boolean complete) {
        this.day_row = 1;
        if (complete) {
            this.updateNextTime();
            this.expire_reward = 0L;
        } else {
            this.next_reward = 0L;
            this.expire_reward = System.currentTimeMillis() + this.getNextTime(System.currentTimeMillis());
        }
    }

    public void updateRewards() {
        final long cur = System.currentTimeMillis();
        if (this.expire_reward > 0L && cur >= this.expire_reward) {
            this.resetRewards(false);
        }
    }

    public void takeReward() {
        ++this.day_row;
        if (this.day_row > Config.opt_days_row) {
            this.resetRewards(true);
        } else {
            this.updateNextTime();
            this.expire_reward = this.next_reward + this.getNextTime(this.next_reward);
        }
    }

    public boolean hasActiveReward() {
        this.updateRewards();
        final long cur = System.currentTimeMillis();
        return (this.expire_reward <= 0L || cur < this.expire_reward) && (this.next_reward <= 0L || cur >= this.next_reward);
    }

    public long getNextTime(final long from) {
        if (Config.opt_midnight) {
            final Calendar cal2 = Calendar.getInstance();
            cal2.setTimeInMillis(from);
            cal2.add(5, 1);
            cal2.set(11, 0);
            cal2.set(12, 0);
            cal2.set(13, 0);
            cal2.set(14, 0);
            final long diff = cal2.getTimeInMillis() - from;
            return diff;
        }
        return Config.opt_cd * 1000L;
    }

    public boolean pastLoginDurationThreshold() {
        // Get the current system time
        long currentTime = System.currentTimeMillis();

        // Calculate the login time plus the duration threshold
        long thresholdTime = login + (Config.opt_wm * 1000); // Convert seconds to milliseconds

        // Check if the current time is past the threshold time
        boolean isPastThreshold = currentTime > thresholdTime;

        return isPastThreshold;
    }

    public long remainingLoginDuration() {
        // Get the current system time
        long currentTime = System.currentTimeMillis();

        // Calculate the login time plus the duration threshold
        long thresholdTime = login + (Config.opt_wm * 1000); // Convert seconds to milliseconds

        // Check if the current time is past the threshold time
        long difference = thresholdTime - currentTime;

        return difference;
    }

    public long warmupDurationUntil() {
        // Calculate the login time plus the duration threshold
        long thresholdTime = login + (Config.opt_wm * 1000); // Convert seconds to milliseconds

        return thresholdTime;
    }
}
