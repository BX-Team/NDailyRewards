package me.nonplay.ndailyrewards.cfg;

import org.bukkit.Material;
import me.nonplay.ndailyrewards.gui.ContentType;
import me.nonplay.ndailyrewards.gui.GUIItem;
import java.util.Iterator;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.ChatColor;
import me.nonplay.ndailyrewards.utils.logs.LogUtil;
import me.nonplay.ndailyrewards.utils.logs.LogType;
import me.nonplay.ndailyrewards.utils.ArchUtils;
import org.bukkit.inventory.ItemStack;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Set;
import org.bukkit.configuration.InvalidConfigurationException;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;

public class JYML extends YamlConfiguration
{
    private File f;

    public JYML(final String path, final String file) {
        this.f = new File(path, file);
        try {
            this.load(this.f);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
        catch (InvalidConfigurationException e3) {
            e3.printStackTrace();
        }
    }

    public JYML(final File f) {
        this.f = f;
        try {
            this.load(this.f);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
        catch (InvalidConfigurationException e3) {
            e3.printStackTrace();
        }
    }

    public Set<String> getSection(final String path) {
        if (!this.isConfigurationSection(path)) {
            return Collections.emptySet();
        }
        return (Set<String>)this.getConfigurationSection(path).getKeys(false);
    }

    public static List<JYML> getFilesFolder(final String path) {
        final List<JYML> names = new ArrayList<JYML>();
        final File folder = new File(path);
        final File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            return names;
        }
        File[] array;
        for (int length = (array = listOfFiles).length, i = 0; i < length; ++i) {
            final File f = array[i];
            if (f.isFile()) {
                names.add(new JYML(f));
            }
            else if (f.isDirectory()) {
                names.addAll(getFilesFolder(f.getPath()));
            }
        }
        return names;
    }

    public ItemStack getItemFromSection(String path) {
        if (!path.endsWith(".")) {
            path = String.valueOf(path) + ".";
        }
        final String mat = this.getString(String.valueOf(path) + "material");
        ItemStack item = ArchUtils.buildItem(mat);
        if (item == null) {
            LogUtil.send("Invalid item material on &f'" + path + "'!" + " &c(" + this.f.getName() + ")", LogType.ERROR);
            return null;
        }
        final String hash = this.getString(String.valueOf(path) + "skull-hash");
        if (hash != null) {
            final String[] ss = path.split("\\.");
            final String id = ss[ss.length - 1];
            item = ArchUtils.getHashed(item, hash, id);
        }
        final ItemMeta meta = item.getItemMeta();
        final String name = this.getString(String.valueOf(path) + "name");
        if (name != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        }
        final List<String> lore = new ArrayList<String>();
        for (final String s : this.getStringList(String.valueOf(path) + "lore")) {
            lore.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        meta.setLore((List)lore);
        if (this.getBoolean(String.valueOf(path) + "enchanted")) {
            meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
        }
        final List<String> flags = (List<String>)this.getStringList(String.valueOf(path) + "item-flags");
        if (flags.contains("*")) {
            meta.addItemFlags(ItemFlag.values());
        }
        else {
            for (final String flag : flags) {
                try {
                    meta.addItemFlags(new ItemFlag[] { ItemFlag.valueOf(flag.toUpperCase()) });
                }
                catch (IllegalArgumentException ex) {}
            }
        }
        item.setItemMeta(meta);
        return item;
    }

    public GUIItem getGUIItemFromSection(String path) {
        if (!path.endsWith(".")) {
            path = String.valueOf(path) + ".";
        }
        final ItemStack item = this.getItemFromSection(path);
        if (item == null) {
            LogUtil.send("Invalid item material on &f'" + path + "'!" + " &c(" + this.f.getName() + ")", LogType.ERROR);
            return null;
        }
        final ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        int[] slots = { 0 };
        if (this.contains(String.valueOf(path) + "slots")) {
            final String[] raw = this.getString(String.valueOf(path) + "slots").replaceAll("\\s", "").split(",");
            slots = new int[raw.length];
            for (int i = 0; i < raw.length; ++i) {
                try {
                    slots[i] = Integer.parseInt(raw[i].trim());
                }
                catch (NumberFormatException ex2) {}
            }
        }
        ContentType type;
        try {
            type = ContentType.valueOf(this.getString(String.valueOf(path) + "type", "NONE"));
        }
        catch (IllegalArgumentException ex) {
            type = ContentType.NONE;
        }
        final String[] ss = path.split("\\.");
        String id = ss[ss.length - 1];
        if (id.isEmpty()) {
            id = String.valueOf(this.f.getName().replace(".yml", "")) + "-icon-" + ArchUtils.randInt(0, 3000);
        }
        final GUIItem gi = new GUIItem(id, type, item, slots);
        return gi;
    }

    public void saveItemToSection(final ItemStack item, String path) {
        if (item == null) {
            return;
        }
        if (!path.endsWith(".")) {
            path = String.valueOf(path) + ".";
        }
        final Material m = item.getType();
        final ItemMeta meta = item.getItemMeta();
        final int data = item.getDurability();
        final String mat = String.valueOf(m.name()) + ":" + data + ":" + item.getAmount();
        this.set(String.valueOf(path) + "material", (Object)mat);
        if (meta.hasDisplayName()) {
            this.set(String.valueOf(path) + "name", (Object)meta.getDisplayName());
        }
        if (meta.hasLore()) {
            this.set(String.valueOf(path) + "lore", (Object)meta.getLore());
        }
        final String hash = ArchUtils.getHashOf(item);
        if (hash != null && !hash.isEmpty()) {
            this.set(String.valueOf(path) + "skull-hash", (Object)hash);
        }
        if (meta.hasEnchants()) {
            this.set(String.valueOf(path) + "enchanted", (Object)true);
        }
        final List<String> f2 = new ArrayList<String>();
        final Set<ItemFlag> flags = (Set<ItemFlag>)meta.getItemFlags();
        for (final ItemFlag f3 : flags) {
            f2.add(f3.name());
        }
        this.set(String.valueOf(path) + "item-flags", (Object)f2);
        this.set(String.valueOf(path) + "unbreakable", (Object)meta.isUnbreakable());
    }

    public void addMissing(final String path, final Object val) {
        if (this.contains(path)) {
            return;
        }
        this.set(path, val);
    }

    public File getFile() {
        return this.f;
    }

    public void save() {
        try {
            this.save(this.f);
        }
        catch (IOException e) {
            LogUtil.send("Unable to save config: &f" + this.f.getName() + "&7! &c(" + e.getMessage() + ")", LogType.ERROR);
        }
    }
}