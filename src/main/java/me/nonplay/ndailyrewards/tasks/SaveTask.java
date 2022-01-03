package me.nonplay.ndailyrewards.tasks;

import org.bukkit.plugin.Plugin;
import me.nonplay.ndailyrewards.cfg.Config;
import me.nonplay.ndailyrewards.NDailyRewards;

public class SaveTask
{
    private NDailyRewards plugin;
    private int id;

    public SaveTask(final NDailyRewards plugin) {
        this.plugin = plugin;
    }

    public void stop() {
        this.plugin.getServer().getScheduler().cancelTask(this.id);
    }

    public void start() {
        this.id = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, (Runnable)new Runnable() {
            @Override
            public void run() {
                SaveTask.this.plugin.getUserManager().autosave();
            }
        }, 0L, Config.data_save * 60L * 1000L);
    }
}