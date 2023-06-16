package gq.bxteam.ndailyrewards.cfg;

import gq.bxteam.ndailyrewards.gui.ContentType;
import gq.bxteam.ndailyrewards.gui.GUIItem;
import gq.bxteam.ndailyrewards.utils.ArchUtils;
import gq.bxteam.ndailyrewards.utils.logs.LogType;
import gq.bxteam.ndailyrewards.utils.logs.LogUtil;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Set;
import org.bukkit.configuration.InvalidConfigurationException;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Integration with plugin GUI and config
 */
public class JYML extends YamlConfiguration
{
    private final File f;

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
        return this.getConfigurationSection(path).getKeys(false);
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
            path = path + ".";
        }
        final String mat = this.getString(path + "material");
        ItemStack item = ArchUtils.buildItem(mat);
        if (item == null) {
            LogUtil.send("Invalid item material on &f'" + path + "'!" + " &c(" + this.f.getName() + ")", LogType.ERROR);
            return null;
        }
        final String hash = this.getString(path + "skull-hash");
        if (hash != null) {
            final String[] ss = path.split("\\.");
            final String id = ss[ss.length - 1];
            item = ArchUtils.getHashed(item, hash, id);
        }
        final ItemMeta meta = item.getItemMeta();
        String name = this.getString(path + "name");
        if (name != null) {
            Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
            Matcher matcher = pattern.matcher(name);
            while (matcher.find()) {
                String hexCode = name.substring(matcher.start(), matcher.end());
                String replaceSharp = hexCode.replace('#', 'x');
                char[] ch = replaceSharp.toCharArray();
                StringBuilder builder = new StringBuilder("");
                for (char c : ch)
                    builder.append("&" + c);
                name = name.replace(hexCode, builder.toString());
                matcher = pattern.matcher(name);
            }
            String pref = ChatColor.translateAlternateColorCodes('&', name);
            meta.setDisplayName(pref);
        }
        final List<String> lore = new ArrayList<String>();
        for (String s : this.getStringList(path + "lore")) {
            Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
            Matcher matcher = pattern.matcher(s);
            while (matcher.find()) {
                String hexCode = s.substring(matcher.start(), matcher.end());
                String replaceSharp = hexCode.replace('#', 'x');
                char[] ch = replaceSharp.toCharArray();
                StringBuilder builder = new StringBuilder("");
                for (char c : ch)
                    builder.append("&" + c);
                s = s.replace(hexCode, builder.toString());
                matcher = pattern.matcher(s);
            }
            String pref = ChatColor.translateAlternateColorCodes('&', s);
            lore.add(pref);
        }
        meta.setLore(lore);
        if (this.getBoolean(path + "enchanted")) {
            meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
        }
        final List<String> flags = this.getStringList(path + "item-flags");
        if (flags.contains("*")) {
            meta.addItemFlags(ItemFlag.values());
        }
        else {
            for (final String flag : flags) {
                try {
                    meta.addItemFlags(ItemFlag.valueOf(flag.toUpperCase()));
                }
                catch (IllegalArgumentException ex) {}
            }
        }
        if (this.isSet(path + "custom-model-data")){
            final int customModelData = this.getInt(path + "custom-model-data");
            meta.setCustomModelData(customModelData);
        }
        item.setItemMeta(meta);
        return item;
    }

    public GUIItem getGUIItemFromSection(String path) {
        if (!path.endsWith(".")) {
            path = path + ".";
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
        if (this.contains(path + "slots")) {
            final String[] raw = this.getString(path + "slots").replaceAll("\\s", "").split(",");
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
            type = ContentType.valueOf(this.getString(path + "type", "NONE"));
        }
        catch (IllegalArgumentException ex) {
            type = ContentType.NONE;
        }
        final String[] ss = path.split("\\.");
        String id = ss[ss.length - 1];
        if (id.isEmpty()) {
            id = this.f.getName().replace(".yml", "") + "-icon-" + ArchUtils.randInt(0, 3000);
        }
        final GUIItem gi = new GUIItem(id, type, item, slots);
        return gi;
    }

    public void saveItemToSection(final ItemStack item, String path) {
        if (item == null) {
            return;
        }
        if (!path.endsWith(".")) {
            path = path + ".";
        }
        final Material m = item.getType();
        final ItemMeta meta = item.getItemMeta();
        final int data = item.getDurability();
        final String mat = m.name() + ":" + data + ":" + item.getAmount();
        this.set(path + "material", mat);
        if (meta.hasDisplayName()) {
            this.set(path + "name", meta.getDisplayName());
        }
        if (meta.hasLore()) {
            this.set(path + "lore", meta.getLore());
        }
        final String hash = ArchUtils.getHashOf(item);
        if (hash != null && !hash.isEmpty()) {
            this.set(path + "skull-hash", hash);
        }
        if (meta.hasEnchants()) {
            this.set(path + "enchanted", true);
        }
        final List<String> f2 = new ArrayList<String>();
        final Set<ItemFlag> flags = meta.getItemFlags();
        for (final ItemFlag f3 : flags) {
            f2.add(f3.name());
        }
        this.set(path + "item-flags", f2);
        this.set(path + "unbreakable", meta.isUnbreakable());
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