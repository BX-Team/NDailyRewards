package gq.bxteam.ndailyrewards.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import java.util.LinkedHashMap;
import java.util.UUID;
import org.bukkit.inventory.InventoryHolder;
import gq.bxteam.ndailyrewards.NDailyRewards;
import gq.bxteam.ndailyrewards.AbstractListener;

public abstract class GUI extends AbstractListener<NDailyRewards> implements InventoryHolder
{
    private final UUID uuid;
    protected String title;
    protected int size;
    protected LinkedHashMap<String, GUIItem> items;
    
    public GUI(final NDailyRewards plugin, final String title, final int size, final LinkedHashMap<String, GUIItem> items) {
        super(plugin);
        this.uuid = UUID.randomUUID();
        this.setTitle(title);
        this.setSize(size);
        final LinkedHashMap<String, GUIItem> map = new LinkedHashMap<String, GUIItem>();
        for (final GUIItem gi : items.values()) {
            map.put(gi.getId(), new GUIItem(gi));
        }
        this.items = map;
        this.registerListeners();
    }
    
    public void shutdown() {
        this.unregisterListeners();
    }
    
    public UUID getUUID() {
        return this.uuid;
    }
    
    protected ItemStack getItem(final Inventory inv, final int slot) {
        final ItemStack i = inv.getItem(slot);
        if (i == null) {
            return new ItemStack(Material.AIR);
        }
        return new ItemStack(i);
    }
    
    public final Inventory getInventory() {
        return this.plugin.getServer().createInventory(this, this.getSize(), this.getTitle());
    }
    
    public void open(final Player p) {
        p.openInventory(this.addDefaults());
    }
    
    protected boolean ignoreNullClick() {
        return true;
    }
    
    protected final Inventory addDefaults() {
        final Inventory inv = this.getInventory();
        for (final GUIItem gi : this.getContent().values()) {
            int[] slots;
            for (int length = (slots = gi.getSlots()).length, i = 0; i < length; ++i) {
                final int slot = slots[i];
                inv.setItem(slot, gi.getItem());
            }
        }
        return inv;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(final String title) {
        this.title = title;
    }
    
    public int getSize() {
        return this.size;
    }
    
    public void setSize(final int size) {
        this.size = size;
    }
    
    public LinkedHashMap<String, GUIItem> getContent() {
        return this.items;
    }
    
    public boolean click(final Player p, final ItemStack item, final ContentType type, final int slot, final InventoryClickEvent e) {
        if (type == ContentType.EXIT) {
            p.closeInventory();
            return false;
        }
        return true;
    }
    
    public boolean onClose(final Player p, final InventoryCloseEvent e) {
        return true;
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onClick(final InventoryClickEvent e) {
        final InventoryHolder ih = e.getInventory().getHolder();
        if (ih == null || !ih.getClass().isInstance(this)) {
            return;
        }
        e.setCancelled(true);
        final GUI g = (GUI)ih;
        if (!g.getUUID().equals(this.uuid)) {
            return;
        }
        final ItemStack item = e.getCurrentItem();
        if (this.ignoreNullClick() && (item == null || item.getType() == Material.AIR)) {
            return;
        }
        this.click((Player)e.getWhoClicked(), item, GUIUtils.getItemType(item), e.getRawSlot(), e);
    }
    
    @EventHandler
    public void onClose(final InventoryCloseEvent e) {
        final InventoryHolder ih = e.getInventory().getHolder();
        if (ih == null || !ih.getClass().isInstance(this)) {
            return;
        }
        final GUI g = (GUI)ih;
        if (!g.getUUID().equals(this.uuid)) {
            return;
        }
        this.onClose((Player)e.getPlayer(), e);
    }
}
