package org.bxteam.ndailyrewards;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bxteam.ndailyrewards.configuration.Messages;
import org.bxteam.ndailyrewards.configuration.PluginConfig;
import org.bxteam.ndailyrewards.manager.reward.ActionsExecutor;
import org.bxteam.quark.common.QuarkLogger;
import org.bxteam.quark.paper.PaperPlatform;
import org.bxteam.quark.paper.PaperScheduler;
import org.bxteam.quark.update.UpdateChecker;

import java.nio.file.Path;

/**
 * The configs are bound as instances: a reload copies the new values into the same objects, so injected
 * references stay current.
 */
@RequiredArgsConstructor
public class NDailyRewardsModule extends AbstractModule {
    private final NDailyRewards plugin;
    private final PaperPlatform platform;
    private final QuarkLogger logger;
    private final PluginConfig config;
    private final Messages messages;
    private final UpdateChecker updateChecker;

    @Override
    protected void configure() {
        this.bind(Plugin.class).toInstance(plugin);
        this.bind(NDailyRewards.class).toInstance(plugin);
        this.bind(PluginManager.class).toInstance(plugin.getServer().getPluginManager());
        this.bind(Server.class).toInstance(plugin.getServer());
        this.bind(PluginDescriptionFile.class).toInstance(plugin.getDescription());
        this.bind(Path.class).annotatedWith(Names.named("dataFolder")).toInstance(platform.dataDirectory());

        this.bind(PaperScheduler.class).toInstance(platform.scheduler());
        this.bind(QuarkLogger.class).toInstance(logger);
        this.bind(PluginConfig.class).toInstance(config);
        this.bind(Messages.class).toInstance(messages);
        this.bind(UpdateChecker.class).toInstance(updateChecker);

        install(new FactoryModuleBuilder().build(ActionsExecutor.Factory.class));
    }
}
