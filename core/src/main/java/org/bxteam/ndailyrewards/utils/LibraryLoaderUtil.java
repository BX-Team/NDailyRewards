package org.bxteam.ndailyrewards.utils;

import org.bukkit.plugin.java.JavaPlugin;
import org.bxteam.quark.bukkit.BukkitLibraryManager;
import org.bxteam.quark.dependency.Dependency;

import java.util.List;

public class LibraryLoaderUtil {
    private static final List<Dependency> DEPENDENCIES = List.of(
            Dependency.of("io.papermc", "paperlib", "1.0.8"),
            Dependency.of("com.zaxxer", "HikariCP", "7.0.0"),
            Dependency.of("org.mariadb.jdbc", "mariadb-java-client", "3.5.4"),
            Dependency.of("com.j256.ormlite", "ormlite-core", "6.1"),
            Dependency.of("com.j256.ormlite", "ormlite-jdbc", "6.1"),
            Dependency.of("aopalliance", "aopalliance", "1.0"),
            Dependency.of("com.google.inject", "guice", "5.1.0"),
            Dependency.of("com.google.inject.extensions", "guice-assistedinject", "5.1.0"),
            Dependency.of("dev.rollczi", "litecommands-bukkit", "3.10.0"),
            Dependency.of("dev.rollczi", "litecommands-folia", "3.10.0")
    );

    public static void loadDependencies(JavaPlugin plugin) {
        BukkitLibraryManager libraryManager = new BukkitLibraryManager(plugin);

        libraryManager.addRepository("https://repo.papermc.io/repository/maven-public/");
        libraryManager.addRepository("https://repo.panda-lang.org/releases/");
        libraryManager.optimizeDependencyDownloads();

        libraryManager.loadDependencies(DEPENDENCIES);
    }
}
