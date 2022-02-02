package me.nonplay.ndailyrewards.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import java.util.LinkedHashMap;
import me.nonplay.ndailyrewards.NDailyRewards;

public abstract class Pageable extends GUI
{
    protected int pages;
    
    public Pageable(final NDailyRewards plugin, final String title, final int size, final LinkedHashMap<String, GUIItem> items, final int pages) {
        super(plugin, title, size, items);
        this.pages = pages;
    }
    
    public final int getPages() {
        return this.pages;
    }
    
    @Override
    public boolean click(final Player p, final ItemStack item, final ContentType type, final int slot, final InventoryClickEvent e) {
        if (!super.click(p, item, type, slot, e)) {
            return false;
        }
        if (type == ContentType.NEXT) {
            final int page = GUIUtils.getPage(item);
            this.open(p, page);
            return false;
        }
        if (type == ContentType.BACK) {
            final int page = GUIUtils.getPage(item);
            this.open(p, page);
            return false;
        }
        return true;
    }
    
    public abstract void open(final Player p0, final int p1);
    
    @Override
    public final void open(final Player p) {
        this.open(p, 1);
    }
    
    protected Inventory addDefaults(final int page) {
        final Inventory inv = this.getInventory();
        for (final GUIItem gi : this.getContent().values()) {
            ItemStack item = gi.getItem().clone();
            if (gi.getType() == ContentType.NEXT) {
                if (this.pages <= 1 || page >= this.pages) {
                    continue;
                }
                item = GUIUtils.setPage(item, page + 1);
            }
            if (gi.getType() == ContentType.BACK) {
                if (page <= 1) {
                    continue;
                }
                item = GUIUtils.setPage(item, page - 1);
            }
            int[] slots;
            for (int length = (slots = gi.getSlots()).length, i = 0; i < length; ++i) {
                final int slot = slots[i];
                inv.setItem(slot, item);
            }
        }
        return inv;
    }
}
