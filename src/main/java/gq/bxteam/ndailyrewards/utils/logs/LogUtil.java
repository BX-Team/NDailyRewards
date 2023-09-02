package gq.bxteam.ndailyrewards.utils.logs;

import gq.bxteam.ndailyrewards.utils.TextUtils;
import org.bukkit.ChatColor;
import gq.bxteam.ndailyrewards.NDailyRewards;

public class LogUtil {
    private static NDailyRewards plugin;

    static {
        LogUtil.plugin = NDailyRewards.instance;
    }

    public static void send(final String msg, final LogType type) {
        String out = type.color() + "[" + type.name() + "] &bNDailyRewards: " + ChatColor.GRAY + msg;
        out = TextUtils.applyColor(out);
        LogUtil.plugin.getServer().getConsoleSender().sendMessage(out);
    }
}
