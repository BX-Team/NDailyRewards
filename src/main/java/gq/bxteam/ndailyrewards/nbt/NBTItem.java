package gq.bxteam.ndailyrewards.nbt;

import java.util.function.BiConsumer;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import gq.bxteam.ndailyrewards.nbt.iface.ReadWriteItemNBT;
import gq.bxteam.ndailyrewards.nbt.iface.ReadWriteNBT;
import gq.bxteam.ndailyrewards.nbt.iface.ReadableNBT;
import gq.bxteam.ndailyrewards.nbt.utils.nmsmappings.ReflectionMethod;

/**
 * NBT class to access vanilla/custom tags on ItemStacks. This class doesn't
 * autosave to the Itemstack, use getItem to get the changed ItemStack
 * 
 * @author tr7zw
 *
 */
public class NBTItem extends NBTCompound implements ReadWriteItemNBT {

    private ItemStack bukkitItem;
    private boolean directApply;
    private ItemStack originalSrcStack = null;

    /**
     * Constructor for NBTItems. The ItemStack will be cloned!
     * 
     * @param item
     */
    public NBTItem(ItemStack item) {
        this(item, false);
    }

    /**
     * Constructor for NBTItems. The ItemStack will be cloned! If directApply is
     * true, all changed will be mapped to the original item. Changes to the NBTItem
     * will overwrite changes done to the original item in that case.
     * 
     * @param item
     * @param directApply
     */
    public NBTItem(ItemStack item, boolean directApply) {
        super(null, null);
        if (item == null || item.getType() == Material.AIR || item.getAmount() <= 0) {
            throw new NullPointerException("ItemStack can't be null/air/amount of 0! This is not a NBTAPI bug!");
        }
        this.directApply = directApply;
        bukkitItem = item.clone();
        if (directApply) {
            this.originalSrcStack = item;
        }
    }

    @Override
    public Object getCompound() {
        return NBTReflectionUtil.getItemRootNBTTagCompound(ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, bukkitItem));
    }

    @Override
    protected void setCompound(Object compound) {
        Object stack = ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, bukkitItem);
        ReflectionMethod.ITEMSTACK_SET_TAG.run(stack, compound);
        bukkitItem = (ItemStack) ReflectionMethod.ITEMSTACK_BUKKITMIRROR.run(null, stack);
    }

    /**
     * Apply stored NBT tags to the provided ItemStack.
     * <p>
     * Note: This will completely override current item's {@link ItemMeta}. If you
     * still want to keep the original item's NBT tags, see
     * {@link #mergeNBT(ItemStack)} and {@link #mergeCustomNBT(ItemStack)}.
     *
     * @param item ItemStack that should get the new NBT data
     */
    public void applyNBT(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            throw new NullPointerException("ItemStack can't be null/Air! This is not a NBTAPI bug!");
        }
        NBTItem nbti = new NBTItem(new ItemStack(item.getType()));
        nbti.mergeCompound(this);
        item.setItemMeta(nbti.getItem().getItemMeta());
    }

    /**
     * Merge all NBT tags to the provided ItemStack.
     *
     * @param item ItemStack that should get the new NBT data
     */
    public void mergeNBT(ItemStack item) {
        NBTItem nbti = new NBTItem(item);
        nbti.mergeCompound(this);
        item.setItemMeta(nbti.getItem().getItemMeta());
    }

    /**
     * Merge only custom (non-vanilla) NBT tags to the provided ItemStack.
     *
     * @param item ItemStack that should get the new NBT data
     */
    public void mergeCustomNBT(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            throw new NullPointerException("ItemStack can't be null/Air!");
        }
        ItemMeta meta = item.getItemMeta();
        NBTReflectionUtil.getUnhandledNBTTags(meta)
                .putAll(NBTReflectionUtil.getUnhandledNBTTags(bukkitItem.getItemMeta()));
        item.setItemMeta(meta);
    }

    /**
     * True, if the item has any tags now known for this item type.
     * 
     * @return true when custom tags are present
     */
    public boolean hasCustomNbtData() {
        ItemMeta meta = bukkitItem.getItemMeta();
        return !NBTReflectionUtil.getUnhandledNBTTags(meta).isEmpty();
    }

    /**
     * Remove all custom (non-vanilla) NBT tags from the NBTItem.
     */
    public void clearCustomNBT() {
        ItemMeta meta = bukkitItem.getItemMeta();
        NBTReflectionUtil.getUnhandledNBTTags(meta).clear();
        bukkitItem.setItemMeta(meta);
    }

    /**
     * @return The modified ItemStack
     */
    public ItemStack getItem() {
        return bukkitItem;
    }

    protected void setItem(ItemStack item) {
        bukkitItem = item;
    }

    /**
     * This may return true even when the NBT is empty.
     * 
     * @return Does the ItemStack have a NBTCompound.
     */
    public boolean hasNBTData() {
        return getCompound() != null;
    }

    /**
     * Gives save access to the {@link ItemMeta} of the internal {@link ItemStack}.
     * Supported operations while inside this scope: - any get/set method of
     * {@link ItemMeta} - any getter on {@link NBTItem}
     * 
     * All changes made to the {@link NBTItem} during this scope will be reverted at
     * the end.
     * 
     * @param handler
     */
    public void modifyMeta(BiConsumer<ReadableNBT, ItemMeta> handler) {
        ItemMeta meta = bukkitItem.getItemMeta();
        handler.accept(this, meta);
        bukkitItem.setItemMeta(meta);
        if (directApply) {
            applyNBT(originalSrcStack);
        }
    }

    /**
     * Gives save access to the {@link ItemMeta} of the internal {@link ItemStack}.
     * Supported operations while inside this scope: - any get/set method of
     * {@link ItemMeta} - any getter on {@link NBTItem}
     * 
     * All changes made to the {@link NBTItem} during this scope will be reverted at
     * the end.
     * 
     * @param handler
     */
    public <T extends ItemMeta> void modifyMeta(Class<T> type, BiConsumer<ReadableNBT, T> handler) {
        @SuppressWarnings("unchecked")
        T meta = (T) bukkitItem.getItemMeta();
        handler.accept(this, meta);
        bukkitItem.setItemMeta(meta);
        if (directApply) {
            applyNBT(originalSrcStack);
        }
    }

    /**
     * Helper method that converts {@link ItemStack} to {@link NBTContainer} with
     * all it's data like Material, Damage, Amount and Tags.
     * 
     * @param item
     * @return Standalone {@link NBTContainer} with the Item's data
     */
    public static NBTContainer convertItemtoNBT(ItemStack item) {
        return NBTReflectionUtil.convertNMSItemtoNBTCompound(ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, item));
    }

    /**
     * Helper method to do the inverse of "convertItemtoNBT". Creates an
     * {@link ItemStack} using the {@link NBTCompound}
     * 
     * @param comp
     * @return ItemStack using the {@link NBTCompound}'s data
     */
    public static ItemStack convertNBTtoItem(NBTCompound comp) {
        return (ItemStack) ReflectionMethod.ITEMSTACK_BUKKITMIRROR.run(null,
                NBTReflectionUtil.convertNBTCompoundtoNMSItem(comp));
    }

    /**
     * Helper method that converts {@link ItemStack}[] to {@link NBTContainer} with
     * all it data like Material, Damage, Amount and Tags. This is a custom
     * implementation and won't work with vanilla code(Shulker content etc).
     * 
     * @param items
     * @return Standalone {@link NBTContainer} with the Item's data
     */
    public static NBTContainer convertItemArraytoNBT(ItemStack[] items) {
        NBTContainer container = new NBTContainer();
        container.setInteger("size", items.length);
        NBTCompoundList list = container.getCompoundList("items");
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            NBTListCompound entry = list.addCompound();
            entry.setInteger("Slot", i);
            entry.mergeCompound(convertItemtoNBT(item));
        }
        return container;
    }

    /**
     * Helper method to do the inverse of "convertItemArraytoNBT". Creates an
     * {@link ItemStack}[] using the {@link NBTCompound}. This is a custom
     * implementation and won't work with vanilla code(Shulker content etc).
     * 
     * Will return null for invalid data. Empty slots in the array are filled with
     * AIR Stacks!
     * 
     * @param comp
     * @return ItemStack[] using the {@link NBTCompound}'s data
     */
    public static ItemStack[] convertNBTtoItemArray(NBTCompound comp) {
        if (!comp.hasTag("size")) {
            return null;
        }
        ItemStack[] rebuild = new ItemStack[comp.getInteger("size")];
        for (int i = 0; i < rebuild.length; i++) { // not using Arrays.fill, since then it's all the same instance
            rebuild[i] = new ItemStack(Material.AIR);
        }
        if (!comp.hasTag("items")) {
            return rebuild;
        }
        NBTCompoundList list = comp.getCompoundList("items");
        for (ReadWriteNBT lcomp : list) {
            if (lcomp instanceof NBTCompound) {
                int slot = lcomp.getInteger("Slot");
                rebuild[slot] = convertNBTtoItem((NBTCompound) lcomp);
            }
        }
        return rebuild;
    }

    @Override
    protected void saveCompound() {
        if (directApply) {
            applyNBT(originalSrcStack);
        }
    }

}
