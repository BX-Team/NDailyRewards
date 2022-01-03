package me.nonplay.ndailyrewards.utils.logs;

import net.md_5.bungee.api.ChatColor;
import me.nonplay.ndailyrewards.NDailyRewards;

public class LogUtil
{
    private static NDailyRewards plugin;
    
    static {
        LogUtil.plugin = NDailyRewards.instance;
    }
    
    public static void send(final String msg, final LogType type) {
        String out = type.color() + "[" + type.name() + "] &bDailyRewards: " + ChatColor.GRAY + msg;
        out = ChatColor.translateAlternateColorCodes('&', out);
        LogUtil.plugin.getServer().getConsoleSender().sendMessage(out);
    }
}
