package space.bxteam.ndailyrewards.gui;

import de.tr7zw.nbtapi.NBTItem;
import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.cfg.Config;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

public class GUIUtils
{
    private static final String key = "archGUI";
    private static final String PAGE = "archGUI_PAGE";
    private static final String ID = "archGUI_ID";
    
    public static ContentType getItemType(final ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return ContentType.NONE;
        }
        final NBTItem nbt = new NBTItem(item);
        ContentType[] values;
        for (int length = (values = ContentType.values()).length, i = 0; i < length; ++i) {
            final ContentType ct = values[i];
            final String cc = ct.name();
            if (nbt.hasKey(cc)) {
                return ct;
            }
        }
        return ContentType.NONE;
    }
    
    public static ItemStack setPage(final ItemStack item, final int page) {
        if (item == null || item.getType() == Material.AIR) {
            return item;
        }
        final NBTItem nbt = new NBTItem(item);
        nbt.setInteger("archGUI_PAGE", page);
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(Config.opt_grd);
                }
                catch (InterruptedException ignored) {}
            }
        }.runTaskAsynchronously(NDailyRewards.getInstance());
        return nbt.getItem();
    }
    
    public static int getPage(final ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return 1;
        }
        final NBTItem nbt = new NBTItem(item);
        if (!nbt.hasKey("archGUI_PAGE")) {
            return 1;
        }
        return nbt.getInteger("archGUI_PAGE");
    }
    
    public static ItemStack setId(final ItemStack item, final String id) {
        if (item == null || item.getType() == Material.AIR) {
            return item;
        }
        final NBTItem nbt = new NBTItem(item);
        nbt.setString("archGUI_ID", id);
        return nbt.getItem();
    }
    
    public static String getId(final ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return "";
        }
        final NBTItem nbt = new NBTItem(item);
        if (!nbt.hasKey("archGUI_ID")) {
            return "";
        }
        return nbt.getString("archGUI_ID");
    }
}
