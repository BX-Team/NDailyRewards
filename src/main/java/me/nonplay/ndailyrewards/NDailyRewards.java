package me.nonplay.ndailyrewards;

import org.bukkit.plugin.java.JavaPlugin;

import me.nonplay.ndailyrewards.data.IDataV2;
import me.nonplay.ndailyrewards.utils.Metrics;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import me.nonplay.ndailyrewards.tasks.SaveTask;
import org.bukkit.command.CommandExecutor;
import me.nonplay.ndailyrewards.manager.UserManager;
import me.nonplay.ndailyrewards.data.DataManager;
import me.nonplay.ndailyrewards.hooks.HookManager;
import org.bukkit.plugin.PluginManager;
import me.nonplay.ndailyrewards.cfg.ConfigManager;
import me.nonplay.ndailyrewards.cmds.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

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
            NDailyRewards.instance = this;
            (this.cmd = new CommandManager(this)).setup();
            this.getCommand("ndailyrewards").setExecutor((CommandExecutor)this.cmd);
            this.pm = this.getServer().getPluginManager();
            (this.hm = new HookManager(this)).setup();
            this.load();
            new SaveTask(this).start();

            // All you have to do is adding the following two lines in your onEnable method.
            // You can find the plugin ids of your plugins on the page https://bstats.org/what-is-my-plugin-id
            int pluginId = 13828; // <-- Replace with the id of your plugin!
            Metrics metrics = new Metrics(this, pluginId);
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
            this.getServer().getScheduler().cancelTasks((Plugin)this);
            HandlerList.unregisterAll((Plugin)this);
            this.um.shutdown();
            this.data.shutdown();
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
    }