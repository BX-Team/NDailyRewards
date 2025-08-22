package org.bxteam.ndailyrewards;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bxteam.helix.logger.ExtendedLogger;
import org.bxteam.helix.updater.ModrinthVersionFetcher;
import org.bxteam.helix.updater.VersionFetcher;
import org.bxteam.ndailyrewards.manager.reward.ActionsExecutor;

import java.nio.file.Path;

@RequiredArgsConstructor
public class NDailyRewardsModule extends AbstractModule {
    private final Plugin plugin;
    private final ExtendedLogger logger;

    @Override
    protected void configure() {
        this.bind(Plugin.class).toInstance(plugin);
        this.bind(NDailyRewards.class).toInstance((NDailyRewards) plugin);
        this.bind(PluginManager.class).toInstance(plugin.getServer().getPluginManager());
        this.bind(Server.class).toInstance(plugin.getServer());
        this.bind(PluginDescriptionFile.class).toInstance(plugin.getDescription());
        this.bind(ExtendedLogger.class).toInstance(logger);
        this.bind(VersionFetcher.class).toInstance(new ModrinthVersionFetcher("ndailyrewards"));
        this.bind(Path.class).annotatedWith(Names.named("dataFolder")).toInstance(plugin.getDataFolder().toPath());

        install(new FactoryModuleBuilder().build(ActionsExecutor.Factory.class));
    }
}
