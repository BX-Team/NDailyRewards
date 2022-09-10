package me.nonplay.ndailyrewards.utils;

import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;

public class MethodNames
{
    private static final MinecraftVersion MINECRAFT_VERSION;
    
    static {
        MINECRAFT_VERSION = MinecraftVersion.getVersion();
    }
    
    public static String getTileDataMethodName() {
        if (MethodNames.MINECRAFT_VERSION == MinecraftVersion.MC1_8_R3) {
            return "b";
        }
        return "save";
    }
    
    public static String getTypeMethodName() {
        if (MethodNames.MINECRAFT_VERSION == MinecraftVersion.MC1_8_R3) {
            return "b";
        }
        return "d";
    }
    
    public static String getEntityNbtGetterMethodName() {
        return "b";
    }
    
    public static String getEntityNbtSetterMethodName() {
        return "a";
    }
    
    public static String getRemoveMethodName() {
        if (MethodNames.MINECRAFT_VERSION == MinecraftVersion.MC1_8_R3) {
            return "a";
        }
        return "remove";
    }
}
