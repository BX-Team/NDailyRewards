package gq.bxteam.ndailyrewards.utils.logs;

import org.bukkit.ChatColor;
import gq.bxteam.ndailyrewards.NDailyRewards;

public class LogUtil {
    private static NDailyRewards plugin;

    static {
        LogUtil.plugin = NDailyRewards.instance;
    }

    public static void send(final String msg, final LogType type) {
        String out = type.color() + "[" + type.name() + "] &bNDailyRewards: " + ChatColor.GRAY + msg;
        out = NDailyRewards.replaceHEXColorCode(out);
        LogUtil.plugin.getServer().getConsoleSender().sendMessage(out);
    }
}
