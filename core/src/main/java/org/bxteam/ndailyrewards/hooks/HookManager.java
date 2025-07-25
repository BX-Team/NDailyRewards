package org.bxteam.ndailyrewards.hooks;

import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.hooks.list.PlaceholderAPIHook;

public class HookManager {
    private final NDailyRewards plugin;

    public HookManager(NDailyRewards plugin) {
        this.plugin = plugin;
    }

    public void registerHooks() {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook().register();
            NDailyRewards.getInstance().getExtendedLogger().info("PlaceholderAPI hook registered");
        }
    }
}
