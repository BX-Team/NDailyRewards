package space.bxteam.ndailyrewards.utils;

import java.lang.reflect.Field;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.GameProfile;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

import space.bxteam.ndailyrewards.cfg.Lang;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;

import org.bukkit.util.StringUtil;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArchUtils {
    public static Random r;

    static {
        ArchUtils.r = new Random();
    }

    public static List<String> getSugg(final String arg, final List<String> source) {
        if (source == null) {
            return null;
        }
        final List<String> ret = new ArrayList<String>();
        final List<String> sugg = new ArrayList<String>(source);
        StringUtil.copyPartialMatches(arg, sugg, (Collection) ret);
        Collections.sort(ret);
        return ret;
    }

    public static void execCmd(String cmd, final Player p) {
        CommandSender cs = p;
        boolean op = false;
        if (cmd.startsWith("op:")) {
            cmd = cmd.replace("op:", "");
            if (!p.isOp()) {
                op = true;
                p.setOp(op);
            }
        } else if (cmd.startsWith("console:")) {
            cmd = cmd.replace("console:", "");
            cs = Bukkit.getConsoleSender();
        }
        cmd = cmd.trim();
        Bukkit.dispatchCommand(cs, cmd.replace("%player%", p.getName()));
        if (op) {
            p.setOp(false);
        }
    }

    public static void addItem(final Player p, final ItemStack item) {
        if (p.getInventory().firstEmpty() == -1) {
            p.getWorld().dropItem(p.getLocation(), item);
        } else {
            p.getInventory().addItem(item);
        }
    }

    public static ItemStack buildItem(final String s) {
        final String[] mat = s.split(":");
        final Material m = Material.getMaterial(mat[0]);
        if (m == null) {
            return null;
        }
        int amount = 1;
        if (mat.length == 3) {
            amount = Integer.parseInt(mat[2]);
        }
        int data = 0;
        if (mat.length >= 2) {
            data = Integer.parseInt(mat[1]);
        }
        final ItemStack item = new ItemStack(m, amount, (short) data);
        return item;
    }

    public static int randInt(int min, int max) {
        final int min2 = min;
        final int max2 = max;
        min = Math.min(min2, max2);
        max = Math.max(min2, max2);
        return ArchUtils.r.nextInt(max - min + 1) + min;
    }

    public static String getTimeLeft(final long max, final long min) {
        final long time = max - min;
        return getTime(time);
    }

    public static String getTimeLeft(final long until) {
        return getTime(until - System.currentTimeMillis());
    }

    public static String getTime(final long time) {
        long secs = time / 1000L;
        long mins = time / 1000L / 60L;
        long hours = mins / 60L;
        long days = hours / 24L;
        secs %= 60L;
        mins %= 60L;
        hours %= 24L;
        days %= 7L;
        String tt = "";
        if (days > 0L) {
            tt = tt + days + " " + Lang.Time_Day.toMsg();
        }
        if (hours > 0L) {
            tt = tt + " " + hours + " " + Lang.Time_Hour.toMsg();
        }
        if (mins > 0L) {
            tt = tt + " " + mins + " " + Lang.Time_Min.toMsg();
        }
        if (tt.isEmpty()) {
            tt = tt + secs + " " + Lang.Time_Sec.toMsg();
        } else if (secs > 0L) {
            tt = tt + " " + secs + " " + Lang.Time_Sec.toMsg();
        }
        return oneSpace(tt);
    }

    public static String oneSpace(final String s) {
        return s.trim().replaceAll("\\s+", " ");
    }

    public static ItemStack getHashed(final ItemStack item, final String hash, final String id) {
        if (hash == null || hash.isEmpty()) {
            return item;
        }
        final UUID uuid = UUID.randomUUID();
        if (item.getType() == Material.PLAYER_HEAD) {
            final SkullMeta sm = (SkullMeta) item.getItemMeta();
            final GameProfile profile = new GameProfile(uuid, null);
            Field profileField = null;
            try {
                profileField = sm.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(sm, profile);
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex2) {
                final Exception ex = null;
                final Exception e1 = ex;
                e1.printStackTrace();
            }
            item.setItemMeta(sm);
        }
        return item;
    }

    public static String getHashOf(final ItemStack item) {
        if (!item.hasItemMeta()) {
            return "";
        }
        if (item.getType() == Material.PLAYER_HEAD) {
            final SkullMeta sm = (SkullMeta) item.getItemMeta();
            Field f = null;
            GameProfile profile;
            try {
                f = sm.getClass().getDeclaredField("profile");
                f.setAccessible(true);
                profile = (GameProfile) f.get(sm);
                f.set(sm, profile);
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex2) {
                final Exception ex = null;
                final Exception e1 = ex;
                e1.printStackTrace();
                return "";
            }
            if (profile == null) {
                return "";
            }
            final Collection<Property> pr = profile.getProperties().get("textures");
            for (final Property p : pr) {
                if (p.getName().equalsIgnoreCase("textures") || p.getSignature().equalsIgnoreCase("textures")) {
                    return p.getValue();
                }
            }
            item.setItemMeta(sm);
        }
        return "";
    }
}
