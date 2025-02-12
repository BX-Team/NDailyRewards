package org.bxteam.ndailyrewards.hooks;

import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.hooks.list.PlaceholderAPIHook;
import org.bxteam.ndailyrewards.utils.LogUtil;

public class HookManager {
    private final NDailyRewards plugin;

    public HookManager(NDailyRewards plugin) {
        this.plugin = plugin;
    }

    public void registerHooks() {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook().register();
            LogUtil.log("PlaceholderAPI hook registered", LogUtil.LogLevel.INFO);
        }
    }
}
