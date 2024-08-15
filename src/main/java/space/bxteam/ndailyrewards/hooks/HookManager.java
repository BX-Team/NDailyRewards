package space.bxteam.ndailyrewards.hooks;

import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.hooks.list.PlaceholderAPIHook;
import space.bxteam.ndailyrewards.utils.LogUtil;

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
