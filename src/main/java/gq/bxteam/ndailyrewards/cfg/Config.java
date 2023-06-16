package gq.bxteam.ndailyrewards.cfg;

import gq.bxteam.ndailyrewards.gui.GUIItem;
import gq.bxteam.ndailyrewards.manager.RewardGUI;
import gq.bxteam.ndailyrewards.manager.objects.Reward;
import gq.bxteam.ndailyrewards.utils.logs.LogType;
import gq.bxteam.ndailyrewards.utils.logs.LogUtil;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gq.bxteam.ndailyrewards.NDailyRewards;
import org.bukkit.ChatColor;
import gq.bxteam.ndailyrewards.data.DataType;

/**
 * Reads and writes config file
 * Also sends warning if you're using snapshot version
 */
public class Config
{
    private static MyConfig config;
    public static int data_save;
    public static DataType storage;
    public static String ms_login;
    public static String ms_pass;
    public static String ms_host;
    public static String ms_base;
    public static boolean ms_purge;
    public static int ms_purge_days;
    public static boolean opt_auto_is;
    public static boolean opt_auto_have;
    public static boolean opt_midnight;
    public static boolean opt_metrics;
    public static int opt_cd;
    public static int opt_days_row;
    public static Map<Integer, Reward> rewards;
    public static RewardGUI rewards_gui;

    public static void setup(final MyConfig mc) {
        Config.config = mc;
        final JYML cfg = Config.config.getConfig();
        cfg.addMissing("options.enable-metrics", true);
        cfg.addMissing("options.unlock-after-midnight", true);
        cfg.addMissing("options.rewards-cool-down", 86400);
        cfg.save();
        Config.data_save = cfg.getInt("data.auto-save", 15);
        String path = "data.storage.";
        final String m = cfg.getString(path + "type").toUpperCase();
        try {
            Config.storage = DataType.valueOf(m);
        }
        catch (IllegalArgumentException ex) {
            Config.storage = DataType.SQLITE;
            LogUtil.send("Unknown storage type: " + m + "! Switched to " + Config.storage.getName(), LogType.WARN);
        }
        Config.ms_login = cfg.getString(path + "username");
        Config.ms_pass = cfg.getString(path + "password");
        Config.ms_host = cfg.getString(path + "host");
        Config.ms_base = cfg.getString(path + "database");
        path = "data.purge.";
        Config.ms_purge = cfg.getBoolean(path + "enabled");
        Config.ms_purge_days = cfg.getInt(path + "days", 60);
        path = "options.";
        Config.opt_auto_is = cfg.getBoolean(path + "auto-open.enabled");
        Config.opt_auto_have = cfg.getBoolean(path + "auto-open.only-when-have");
        Config.opt_midnight = cfg.getBoolean(path + "unlock-after-midnight");
        Config.opt_metrics = cfg.getBoolean(path + "enable-metrics");
        Config.opt_cd = cfg.getInt(path + "rewards-cool-down", 86400);
        Config.opt_days_row = cfg.getInt(path + "days-row");
        Config.rewards = new TreeMap<Integer, Reward>();
        for (final String d : cfg.getSection("rewards")) {
            final int day = Integer.parseInt(d);
            final List<String> lore = cfg.getStringList("rewards." + d + ".lore");
            final List<String> cmd = cfg.getStringList("rewards." + d + ".commands");
            final List<String> msg = cfg.getStringList("rewards." + d + ".messages");
            final Reward r = new Reward(day, lore, cmd, msg);
            Config.rewards.put(day, r);
        }
        LogUtil.send("&eLoaded &6" + Config.rewards.size() + " &edaily rewards!", LogType.INFO);
        path = "gui.";
        String p_title = cfg.getString(path + "title");
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(p_title);
        while (matcher.find()) {
            String hexCode = p_title.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');
            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch)
                builder.append("&" + c);
            p_title = p_title.replace(hexCode, builder.toString());
            matcher = pattern.matcher(p_title);
        }
        String g_title = ChatColor.translateAlternateColorCodes('&', p_title);
        final int g_size = cfg.getInt(path + "size");
        final LinkedHashMap<String, GUIItem> g_items = new LinkedHashMap<String, GUIItem>();
        for (final String id : cfg.getSection(path + "items")) {
            final String path2 = path + "items." + id + ".";
            final GUIItem gi = cfg.getGUIItemFromSection(path2);
            g_items.put(id, gi);
        }
        if (NDailyRewards.getInstance().getDescription().getVersion().contains("SNAPSHOT")) {
            LogUtil.send("&cYou are using a SNAPSHOT version of the plugin! Bugs & Errors may occur!", LogType.WARN);
        }
        int[] slots = { 0 };
        if (cfg.contains(path + "days-positions")) {
            final String[] raw = cfg.getString(path + "days-positions").replaceAll("\\s", "").split(",");
            slots = new int[raw.length];
            for (int i = 0; i < raw.length; ++i) {
                try {
                    slots[i] = Integer.parseInt(raw[i].trim());
                }
                catch (NumberFormatException ex2) {}
            }
        }
        final ItemStack day_ready = cfg.getItemFromSection(path + "days-display.available");
        final ItemStack day_taken = cfg.getItemFromSection(path + "days-display.taken");
        final ItemStack day_locked = cfg.getItemFromSection(path + "days-display.locked");
        final ItemStack day_next = cfg.getItemFromSection(path + "days-display.next");
        Config.rewards_gui = new RewardGUI(NDailyRewards.getInstance(), g_title, g_size, g_items, slots, day_ready, day_taken, day_locked, day_next);
    }

    public static Reward getRewardByDay(final int day) {
        if (Config.rewards.containsKey(day)) {
            return Config.rewards.get(day);
        }
        return null;
    }
}