package space.bxteam.ndailyrewards.gui;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

public class GUIItem
{
    private String id;
    private ContentType type;
    private ItemStack item;
    private int[] slot;
    
    public GUIItem(final String id, final ContentType type, final ItemStack item, final int[] slot) {
        this.setId(id);
        this.setType(type);
        this.setItem(item);
        this.setSlots(slot);
    }
    
    public GUIItem(final GUIItem i2) {
        this.setId(i2.getId());
        this.setType(i2.getType());
        this.setItem(i2.getItem());
        this.setSlots(i2.getSlots());
    }
    
    public String getId() {
        return this.id;
    }
    
    public void setId(final String id) {
        this.id = id;
    }
    
    public ContentType getType() {
        return this.type;
    }
    
    public void setType(final ContentType type) {
        this.type = type;
    }
    
    public ItemStack getItem() {
        return new ItemStack(this.item);
    }
    
    public void setItem(final ItemStack item) {
        if (this.type == ContentType.NONE) {
            this.item = new ItemStack(item);
            return;
        }
        final NBTItem nbt = new NBTItem(item);
        nbt.setString(this.type.name(), this.type.name());
        this.item = new ItemStack(nbt.getItem());
    }
    
    public int[] getSlots() {
        return this.slot;
    }
    
    public void setSlots(final int[] slot) {
        this.slot = slot;
    }
}
