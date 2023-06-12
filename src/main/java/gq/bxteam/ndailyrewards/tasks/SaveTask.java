package gq.bxteam.ndailyrewards.tasks;

import gq.bxteam.ndailyrewards.cfg.Config;
import org.bukkit.plugin.Plugin;
import gq.bxteam.ndailyrewards.NDailyRewards;

public class SaveTask
{
    private final NDailyRewards plugin;
    private int id;

    public SaveTask(final NDailyRewards plugin) {
        this.plugin = plugin;
    }

    public void stop() {
        this.plugin.getServer().getScheduler().cancelTask(this.id);
    }

    public void start() {
        this.id = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {
            @Override
            public void run() {
                SaveTask.this.plugin.getUserManager().autosave();
            }
        }, 0L, Config.data_save * 60L * 1000L);
    }
}