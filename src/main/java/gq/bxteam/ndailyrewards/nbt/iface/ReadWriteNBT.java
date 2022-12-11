package gq.bxteam.ndailyrewards.nbt.iface;

import java.util.UUID;

import org.bukkit.inventory.ItemStack;

public interface ReadWriteNBT extends ReadableNBT {

    /**
     * Merges all data from comp into this compound. This is done in one action, so
     * it also works with Tiles/Entities
     * 
     * @param comp
     */
    void mergeCompound(ReadableNBT comp);

    /**
     * Setter
     * 
     * @param key
     * @param value
     */
    void setString(String key, String value);

    /**
     * Setter
     * 
     * @param key
     * @param value
     */
    void setInteger(String key, Integer value);

    /**
     * Setter
     * 
     * @param key
     * @param value
     */
    void setDouble(String key, Double value);

    /**
     * Setter
     * 
     * @param key
     * @param value
     */
    void setByte(String key, Byte value);

    /**
     * Setter
     * 
     * @param key
     * @param value
     */
    void setShort(String key, Short value);

    /**
     * Setter
     * 
     * @param key
     * @param value
     */
    void setLong(String key, Long value);

    /**
     * Setter
     * 
     * @param key
     * @param value
     */
    void setFloat(String key, Float value);

    /**
     * Setter
     * 
     * @param key
     * @param value
     */
    void setByteArray(String key, byte[] value);

    /**
     * Setter
     * 
     * @param key
     * @param value
     */
    void setIntArray(String key, int[] value);

    /**
     * Setter
     * 
     * @param key
     * @param value
     */
    void setBoolean(String key, Boolean value);

    /**
     * Save an ItemStack as a compound under a given key
     * 
     * @param key
     * @param item
     */
    void setItemStack(String key, ItemStack item);

    /**
     * Save an ItemStack Array as a compound under a given key
     * 
     * @param key
     * @param items
     */
    void setItemStackArray(String key, ItemStack[] items);

    /**
     * Setter
     *
     * @param key
     * @param value
     */
    void setUUID(String key, UUID value);

    /**
     * @param key Deletes the given Key
     */
    void removeKey(String key);

    /**
     * The same as addCompound, just with a name that better reflects what it does
     * 
     * @param name
     * @return
     */
    ReadableNBT getOrCreateCompound(String name);

    /**
     * Set a key to the given Enum value. It gets stored as a String. Passing null
     * as value will call removeKey(key) instead.
     * 
     * @param <E>
     * @param key
     * @param value
     */
    <E extends Enum<?>> void setEnum(String key, E value);

    @Override
    ReadWriteNBTList<String> getStringList(String name);

    @Override
    ReadWriteNBTList<Integer> getIntegerList(String name);

    @Override
    ReadWriteNBTList<int[]> getIntArrayList(String name);

    @Override
    ReadWriteNBTList<UUID> getUUIDList(String name);

    @Override
    ReadWriteNBTList<Float> getFloatList(String name);

    @Override
    ReadWriteNBTList<Double> getDoubleList(String name);

    @Override
    ReadWriteNBTList<Long> getLongList(String name);

    @Override
    ReadWriteNBTCompoundList getCompoundList(String name);

    /**
     * Remove all keys from this compound
     */
    void clearNBT();

}