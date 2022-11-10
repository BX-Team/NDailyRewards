package gq.bxteam.ndailyrewards.nbt;

import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

/**
 * General utility class for a clean and simple nbt access.
 * 
 * @author tr7zw
 *
 */
public class NBT {

    private NBT() {
        // No instances of NBT. Utility class
    }

    public static <T> T get(ItemStack item, Function<NBTCompound, T> getter) {
        return getter.apply(new NBTItem(item));
    }

    public static <T> T get(Entity entity, Function<NBTCompound, T> getter) {
        return getter.apply(new NBTEntity(entity));
    }

    public static <T> T get(BlockState blockState, Function<NBTCompound, T> getter) {
        return getter.apply(new NBTTileEntity(blockState));
    }

    public static <T> T getPersistentData(Entity entity, Function<NBTCompound, T> getter) {
        return getter.apply(new NBTEntity(entity).getPersistentDataContainer());
    }

    public static <T> T getPersistentData(BlockState blockState, Function<NBTCompound, T> getter) {
        return getter.apply(new NBTTileEntity(blockState).getPersistentDataContainer());
    }

    public static <T> T modify(ItemStack item, Function<NBTCompound, T> function) {
        NBTItem nbti = new NBTItem(item, true);
        T val = function.apply(nbti);
        nbti.applyNBT(item);
        return val;
    }

    public static void modify(ItemStack item, Consumer<NBTCompound> consumer) {
        NBTItem nbti = new NBTItem(item, true);
        consumer.accept(nbti);
        nbti.applyNBT(item);
    }

    public static <T> T modify(Entity entity, Function<NBTCompound, T> function) {
        return function.apply(new NBTEntity(entity));
    }

    public static void modify(Entity entity, Consumer<NBTCompound> consumer) {
        consumer.accept(new NBTEntity(entity));
    }

    public static <T> T modifyPersistentData(Entity entity, Function<NBTCompound, T> function) {
        return function.apply(new NBTEntity(entity).getPersistentDataContainer());
    }

    public static void modifyPersistentData(Entity entity, Consumer<NBTCompound> consumer) {
        consumer.accept(new NBTEntity(entity).getPersistentDataContainer());
    }

    public static <T> T modify(BlockState blockState, Function<NBTCompound, T> function) {
        return function.apply(new NBTTileEntity(blockState));
    }

    public static void modify(BlockState blockState, Consumer<NBTCompound> consumer) {
        consumer.accept(new NBTTileEntity(blockState));
    }

    public static <T> T modifyPersistentData(BlockState blockState, Function<NBTCompound, T> function) {
        return function.apply(new NBTTileEntity(blockState).getPersistentDataContainer());
    }

    public static void modifyPersistentData(BlockState blockState, Consumer<NBTCompound> consumer) {
        consumer.accept(new NBTTileEntity(blockState).getPersistentDataContainer());
    }

}
