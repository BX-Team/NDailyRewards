package me.nonplay.ndailyrewards.nbt;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import me.nonplay.ndailyrewards.nbt.utils.nmsmappings.ClassWrapper;
import me.nonplay.ndailyrewards.nbt.utils.nmsmappings.ReflectionMethod;

/**
 * String implementation for NBTLists
 * 
 * @author tr7zw
 *
 */
public class NBTStringList extends NBTList<String> {

	protected NBTStringList(NBTCompound owner, String name, NBTType type, Object list) {
		super(owner, name, type, list);
	}

	@Override
	public String get(int index) {
		try {
			return (String) ReflectionMethod.LIST_GET_STRING.run(listObject, index);
		} catch (Exception ex) {
			throw new NbtApiException(ex);
		}
	}

	@Override
	protected Object asTag(String object) {
		try {
			Constructor<?> con = ClassWrapper.NMS_NBTTAGSTRING.getClazz().getDeclaredConstructor(String.class);
			con.setAccessible(true);
			return con.newInstance(object);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new NbtApiException("Error while wrapping the Object " + object + " to it's NMS object!", e);
		}
	}

}
