package org.bxteam.ndailyrewards.utils;

import org.bukkit.plugin.java.JavaPlugin;
import org.bxteam.quark.bukkit.BukkitLibraryManager;

public class LibraryLoaderUtil {
    public static void loadDependencies(JavaPlugin plugin) {
        BukkitLibraryManager libraryManager = new BukkitLibraryManager(plugin);

        libraryManager.optimizeDependencyDownloads();
        libraryManager.loadFromGradle();
    }
}
