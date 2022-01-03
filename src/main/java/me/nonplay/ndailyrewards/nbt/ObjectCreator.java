package me.nonplay.ndailyrewards.nbt;

import me.nonplay.ndailyrewards.nbt.utils.nmsmappings.ClassWrapper;

import java.lang.reflect.Constructor;

public enum ObjectCreator
{
    NMS_NBTTAGCOMPOUND(ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz(), (Class<?>[])new Class[0]),
    NMS_BLOCKPOSITION(ClassWrapper.NMS_BLOCKPOSITION.getClazz(), (Class<?>[])new Class[] { Integer.TYPE, Integer.TYPE, Integer.TYPE });
    
    private Constructor<?> construct;
    
    private ObjectCreator(final Class<?> clazz, final Class<?>[] args) {
        try {
            this.construct = clazz.getConstructor(args);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public Object getInstance(final Object... args) {
        try {
            return this.construct.newInstance(args);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
