package space.bxteam.ndailyrewards.utils.logs;

import space.bxteam.ndailyrewards.utils.TextUtils;
import org.bukkit.ChatColor;
import space.bxteam.ndailyrewards.NDailyRewards;

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
