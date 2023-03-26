package gq.bxteam.ndailyrewards;

import gq.bxteam.ndailyrewards.cfg.Config;
import gq.bxteam.ndailyrewards.cfg.ConfigManager;
import gq.bxteam.ndailyrewards.cmds.CommandManager;
import gq.bxteam.ndailyrewards.manager.UserManager;
import gq.bxteam.ndailyrewards.tasks.SaveTask;
import gq.bxteam.ndailyrewards.utils.logs.LogType;
import gq.bxteam.ndailyrewards.utils.logs.LogUtil;
import gq.bxteam.ndailyrewards.utils.metrics.Metrics;
import gq.bxteam.ndailyrewards.data.DataManager;
import gq.bxteam.ndailyrewards.hooks.HookManager;
import gq.bxteam.ndailyrewards.data.IDataV2;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class NDailyRewards extends JavaPlugin
{
    public static NDailyRewards instance;
    private CommandManager cmd;
    private ConfigManager cm;
    private PluginManager pm;
    private HookManager hm;
    private DataManager data;
    private UserManager um;

    public static NDailyRewards getInstance() {
        return NDailyRewards.instance;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        NDailyRewards.instance = this;
        (this.cmd = new CommandManager(this)).setup();
        this.getCommand("ndailyrewards").setExecutor((CommandExecutor) this.cmd);
        this.pm = this.getServer().getPluginManager();
        (this.hm = new HookManager(this)).setup();
        this.load();
        new SaveTask(this).start();
        this.MetricsCheck();
    }

    public void onDisable() {
        this.unload();
    }

    public void load() {
        (this.cm = new ConfigManager(this)).setup();
        (this.data = new DataManager(this)).setup();
        (this.um = new UserManager(this)).setup();
    }

    public void unload() {
        try {
            this.getServer().getScheduler().cancelTasks((Plugin) this);
            HandlerList.unregisterAll((Plugin) this);
            this.um.shutdown();
            this.data.shutdown();
        } catch (Exception e) {
            LogUtil.send("&cError while saving plugin data: " + e.getMessage(), LogType.ERROR);
        }
    }

    public void reload() {
        this.unload();
        this.load();
    }

    public PluginManager getPluginManager() {
        return this.pm;
    }

    public IDataV2 getData() {
        return this.data.getData();
    }

    public UserManager getUserManager() {
        return this.um;
    }

    public void MetricsCheck() {
        if (Config.opt_metrics) {
            // Metrics enabled - Initialize metrics
            int pluginId = 13844;
            Metrics metrics = new Metrics(this, pluginId);
        } else {
            // Metrics disabled
        }
    }
}