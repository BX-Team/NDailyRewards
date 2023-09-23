package gq.bxteam.ndailyrewards.tasks;

import gq.bxteam.ndailyrewards.cfg.Config;
import gq.bxteam.ndailyrewards.NDailyRewards;
import org.bukkit.scheduler.BukkitRunnable;

public class SaveTask {
    private final NDailyRewards plugin;
    private int id;

    public SaveTask(final NDailyRewards plugin) {
        this.plugin = plugin;
    }

    public void stop() {
        this.plugin.getServer().getScheduler().cancelTask(this.id);
    }

    public void start() {
        this.id = new BukkitRunnable() {
            @Override
            public void run() {
                SaveTask.this.plugin.getUserManager().autosave();
            }
        }.runTaskTimer(NDailyRewards.getInstance(), 0L, Config.data_save * 60L * 1000L).getTaskId();
    }
}