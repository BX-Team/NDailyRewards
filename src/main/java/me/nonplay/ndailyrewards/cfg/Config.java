package me.nonplay.ndailyrewards.cfg;

import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.Iterator;
import me.nonplay.ndailyrewards.NDailyRewards;
import me.nonplay.ndailyrewards.gui.GUIItem;
import java.util.LinkedHashMap;
import org.bukkit.ChatColor;
import java.util.TreeMap;
import me.nonplay.ndailyrewards.utils.logs.LogUtil;
import me.nonplay.ndailyrewards.utils.logs.LogType;
import me.nonplay.ndailyrewards.manager.RewardGUI;
import me.nonplay.ndailyrewards.manager.objects.Reward;
import java.util.Map;
import me.nonplay.ndailyrewards.data.DataType;

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
    public static boolean cfgver;
    public static int opt_cd;
    public static int opt_days_row;
    public static Map<Integer, Reward> rewards;
    public static RewardGUI rewards_gui;

    public static void setup(final MyConfig mc) {
        Config.config = mc;
        final JYML cfg = Config.config.getConfig();
        cfg.addMissing("options.unlock-after-midnight", true);
        cfg.addMissing("options.rewards-cooldown", 86400);
        cfg.save();
        Config.data_save = cfg.getInt("data.auto-save", 15);
        String path = "data.storage.";
        final String m = cfg.getString(String.valueOf(path) + "type").toUpperCase();
        try {
            Config.storage = DataType.valueOf(m);
        }
        catch (IllegalArgumentException ex) {
            Config.storage = DataType.SQLITE;
            LogUtil.send("Unknown storage type: " + m + "! Switched to " + Config.storage.getName(), LogType.WARN);
        }
        Config.ms_login = cfg.getString(String.valueOf(path) + "username");
        Config.ms_pass = cfg.getString(String.valueOf(path) + "password");
        Config.ms_host = cfg.getString(String.valueOf(path) + "host");
        Config.ms_base = cfg.getString(String.valueOf(path) + "database");
        path = "data.purge.";
        Config.ms_purge = cfg.getBoolean(String.valueOf(path) + "enabled");
        Config.ms_purge_days = cfg.getInt(String.valueOf(path) + "days", 60);
        path = "options.";
        Config.opt_auto_is = cfg.getBoolean(String.valueOf(path) + "auto-open.enabled");
        Config.opt_auto_have = cfg.getBoolean(String.valueOf(path) + "auto-open.only-when-have");
        Config.opt_midnight = cfg.getBoolean(String.valueOf(path) + "unlock-after-midnight");
        Config.opt_cd = cfg.getInt(String.valueOf(path) + "rewards-cooldown", 86400);
        Config.opt_days_row = cfg.getInt(String.valueOf(path) + "days-row");
        Config.cfgver = cfg.getBoolean(String.valueOf(path) + "cfgver");
        Config.rewards = new TreeMap<Integer, Reward>();
        for (final String d : cfg.getSection("rewards")) {
            final int day = Integer.parseInt(d);
            final List<String> lore = (List<String>)cfg.getStringList("rewards." + d + ".lore");
            final List<String> cmd = (List<String>)cfg.getStringList("rewards." + d + ".commands");
            final List<String> msg = (List<String>)cfg.getStringList("rewards." + d + ".messages");
            final Reward r = new Reward(day, lore, cmd, msg);
            Config.rewards.put(day, r);
        }
        LogUtil.send("&eLoaded &6" + Config.rewards.size() + " &edaily rewards!", LogType.INFO);
        path = "gui.";
        final String g_title = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "title"));
        final int g_size = cfg.getInt(String.valueOf(path) + "size");
        final LinkedHashMap<String, GUIItem> g_items = new LinkedHashMap<String, GUIItem>();
        for (final String id : cfg.getSection(String.valueOf(path) + "items")) {
            final String path2 = String.valueOf(path) + "items." + id + ".";
            final GUIItem gi = cfg.getGUIItemFromSection(path2);
            g_items.put(id, gi);
        }
        int[] slots = { 0 };
        if (cfg.contains(String.valueOf(path) + "days-positions")) {
            final String[] raw = cfg.getString(String.valueOf(path) + "days-positions").replaceAll("\\s", "").split(",");
            slots = new int[raw.length];
            for (int i = 0; i < raw.length; ++i) {
                try {
                    slots[i] = Integer.parseInt(raw[i].trim());
                }
                catch (NumberFormatException ex2) {}
            }
        }
        final ItemStack day_ready = cfg.getItemFromSection(String.valueOf(path) + "days-display.available");
        final ItemStack day_taken = cfg.getItemFromSection(String.valueOf(path) + "days-display.taken");
        final ItemStack day_locked = cfg.getItemFromSection(String.valueOf(path) + "days-display.locked");
        final ItemStack day_next = cfg.getItemFromSection(String.valueOf(path) + "days-display.next");
        Config.rewards_gui = new RewardGUI(NDailyRewards.getInstance(), g_title, g_size, g_items, slots, day_ready, day_taken, day_locked, day_next);
    }

    public static Reward getRewardByDay(final int day) {
        if (Config.rewards.containsKey(day)) {
            return Config.rewards.get(day);
        }
        return null;
    }
}