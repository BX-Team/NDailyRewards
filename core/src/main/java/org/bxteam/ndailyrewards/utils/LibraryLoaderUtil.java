package org.bxteam.ndailyrewards.utils;

import org.bukkit.plugin.java.JavaPlugin;
import org.bxteam.quark.bukkit.BukkitLibraryManager;
import org.bxteam.quark.dependency.Dependency;

import java.util.List;

public class LibraryLoaderUtil {
    private static final List<Dependency> DEPENDENCIES = List.of(
            Dependency.of("io.papermc", "paperlib", "1.0.8"),
            Dependency.of("com.zaxxer", "HikariCP", "7.0.2"),
            Dependency.of("org.mariadb.jdbc", "mariadb-java-client", "3.5.5"),
            Dependency.of("com.j256.ormlite", "ormlite-core", "6.1"),
            Dependency.of("com.j256.ormlite", "ormlite-jdbc", "6.1"),
            Dependency.of("aopalliance", "aopalliance", "1.0"),
            Dependency.of("com.google.inject", "guice", "5.1.0"),
            Dependency.of("com.google.inject.extensions", "guice-assistedinject", "5.1.0"),
            Dependency.of("io.github.revxrsal", "lamp.common", "4.0.0-rc.12"),
            Dependency.of("io.github.revxrsal", "lamp.bukkit", "4.0.0-rc.12")
    );

    public static void loadDependencies(JavaPlugin plugin) {
        BukkitLibraryManager libraryManager = new BukkitLibraryManager(plugin);

        libraryManager.addRepository("https://repo.papermc.io/repository/maven-public/");
        libraryManager.optimizeDependencyDownloads();

        libraryManager.loadDependencies(DEPENDENCIES);
    }
}
