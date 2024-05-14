package space.bxteam.ndailyrewards.hooks;

import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.hooks.external.CitizensHK;
import space.bxteam.ndailyrewards.utils.logs.LogType;
import space.bxteam.ndailyrewards.utils.logs.LogUtil;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class HookManager
{
    private final NDailyRewards plugin;
    
    public HookManager(final NDailyRewards plugin) {
        this.plugin = plugin;
    }
    
    public void setup() {
        final PluginManager pm = this.plugin.getPluginManager();
        Hook[] values;
        for (int length = (values = Hook.values()).length, i = 0; i < length; ++i) {
            final Hook h = values[i];
            final Plugin p = pm.getPlugin(h.getPluginName());
            if (p != null) {
                if (p.isEnabled()) {
                    h.enable();
                    LogUtil.send("Hooked with: &b" + h.getPluginName(), LogType.INFO);
                    if (h == Hook.CITIZENS) {
                        CitizensHK.setup();
                    }
                }
            }
        }
    }
}
