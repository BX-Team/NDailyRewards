package org.bxteam.ndailyrewards;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;
import org.bxteam.ndailyrewards.database.DatabaseClient;
import org.bxteam.ndailyrewards.configuration.Messages;
import org.bxteam.ndailyrewards.configuration.PluginConfig;
import org.bxteam.ndailyrewards.database.DatabaseModule;
import org.bxteam.ndailyrewards.integration.IntegrationRegistry;
import org.bxteam.ndailyrewards.listener.InventoryClickListener;
import org.bxteam.ndailyrewards.listener.PlayerJoinListener;
import org.bxteam.ndailyrewards.manager.CommandManager;
import org.bxteam.ndailyrewards.manager.menu.MenuManager;
import org.bxteam.ndailyrewards.manager.reward.RewardManager;
import org.bxteam.ndailyrewards.messaging.MessageService;
import org.bxteam.quark.LibraryManager;
import org.bxteam.quark.bukkit.BukkitUpdateNotifier;
import org.bxteam.quark.common.JulLogger;
import org.bxteam.quark.common.QuarkLogger;
import org.bxteam.quark.config.ConfigException;
import org.bxteam.quark.config.backend.BackendProvider;
import org.bxteam.quark.logging.DebugSwitch;
import org.bxteam.quark.paper.PaperPlatform;
import org.bxteam.quark.update.UpdateChecker;
import org.bxteam.quark.update.UpdateProvider;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

public final class NDailyRewards extends JavaPlugin {
    private boolean librariesLoaded;
    private Injector injector;
    private QuarkLogger logger;
    private PaperPlatform platform;
    private PluginConfig config;
    private Messages messages;
    private CommandManager commandManager;

    @Override
    public void onLoad() {
        try {
            LibraryManager libraryManager = LibraryManager.builder()
                    .plugin(this)
                    .dataDirectory(getDataFolder().toPath())
                    .logger(new JulLogger(getLogger()))
                    .build();
            libraryManager.loadFromGradle();
            // snakeyaml-engine for the configs goes to the same libs/ folder
            BackendProvider.libraryManager(libraryManager);
            this.librariesLoaded = true;
        } catch (LibraryManager.LibraryLoadException e) {
            getLogger().severe("Could not download the libraries NDailyRewards needs: " + e.getMessage()
                    + ". Check the server's internet access, or put the jars into plugins/NDailyRewards/libs by hand.");
        }
    }

    @Override
    public void onEnable() {
        if (!this.librariesLoaded) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Instant startTime = Instant.now();

        this.platform = PaperPlatform.create(this);
        this.logger = DebugSwitch.wrap(this.platform.logger());

        Path dataFolder = this.platform.dataDirectory();
        this.config = PluginConfig.create(dataFolder, this.logger);
        this.messages = Messages.create(dataFolder, this.logger);
        try {
            this.config.load();
            this.messages.load();
        } catch (ConfigException e) {
            this.logger.error(e.getMessage() + " Fix the file and restart the server, NDailyRewards is disabled until then.", null);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        DebugSwitch.setEnabled(this.config.debug);

        UpdateChecker updateChecker = new UpdateChecker(UpdateProvider.modrinth("ndailyrewards"), getDescription().getVersion());

        this.injector = Guice.createInjector(
                new NDailyRewardsModule(this, this.platform, this.logger, this.config, this.messages, updateChecker),
                new DatabaseModule(this.config)
        );

        new Metrics(this, 13844);

        this.logger.info("Loading plugin managers...");
        Logger.setGlobalLogLevel(Level.ERROR);
        try {
            this.injector.getInstance(DatabaseClient.class).open();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        this.injector.getInstance(IntegrationRegistry.class).init();
        MessageService messageService = this.injector.getInstance(MessageService.class);
        messageService.init();
        this.injector.getInstance(RewardManager.class);
        this.injector.getInstance(MenuManager.class);

        this.logger.info("Registering listeners...");
        getServer().getPluginManager().registerEvents(this.injector.getInstance(InventoryClickListener.class), this);
        getServer().getPluginManager().registerEvents(this.injector.getInstance(PlayerJoinListener.class), this);

        this.commandManager = new CommandManager(this, this.injector);
        this.commandManager.registerCommands();

        Duration timeTaken = Duration.between(startTime, Instant.now());
        this.logger.info("Successfully enabled (took {}ms)", timeTaken.toMillis());

        if (this.config.checkUpdates) {
            updateChecker.checkAndLog(this.logger);
            BukkitUpdateNotifier.register(this, updateChecker, "ndailyrewards.update-notify",
                    outdated -> messageService.updateNotice(outdated.latest()));
        }
    }

    @Override
    public void onDisable() {
        if (this.injector == null) {
            return;
        }
        this.platform.scheduler().cancelAll();
        this.commandManager.unregisterCommands();
        this.injector.getInstance(DatabaseClient.class).close();
        this.injector.getInstance(MenuManager.class).shutdown();
        this.injector.getInstance(RewardManager.class).unload();
        this.injector.getInstance(MessageService.class).shutdown();
    }

    /**
     * Re-reads config.yml and lang.yml. A file that fails to load throws and keeps its previous values.
     *
     * @throws ConfigException if a file cannot be read or has invalid values
     */
    public void reload() {
        this.config.reload();
        this.messages.reload();
        DebugSwitch.setEnabled(this.config.debug);

        this.injector.getInstance(RewardManager.class).reload();
    }
}
