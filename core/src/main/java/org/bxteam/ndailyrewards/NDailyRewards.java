package org.bxteam.ndailyrewards;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.Logger;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bxteam.helix.Metrics;
import org.bxteam.helix.database.DatabaseClient;
import org.bxteam.helix.logger.ExtendedLogger;
import org.bxteam.helix.logger.LogLevel;
import org.bxteam.helix.logger.appender.Appender;
import org.bxteam.helix.logger.appender.ConsoleAppender;
import org.bxteam.helix.scheduler.Scheduler;
import org.bxteam.helix.updater.VersionFetcher;
import org.bxteam.ndailyrewards.database.DatabaseModule;
import org.bxteam.ndailyrewards.integration.IntegrationRegistry;
import org.bxteam.ndailyrewards.listener.InventoryClickListener;
import org.bxteam.ndailyrewards.listener.PlayerJoinListener;
import org.bxteam.ndailyrewards.manager.CommandManager;
import org.bxteam.ndailyrewards.manager.menu.MenuManager;
import org.bxteam.ndailyrewards.configuration.Language;
import org.bxteam.ndailyrewards.manager.reward.RewardManager;
import org.bxteam.ndailyrewards.scheduler.SchedulerSetup;
import org.bxteam.ndailyrewards.utils.LibraryLoaderUtil;

import java.io.File;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public final class NDailyRewards extends JavaPlugin {
    private Injector injector;
    private ExtendedLogger logger;
    private File langFile;
    private FileConfiguration langConfig;
    private CommandManager commandManager;

    public FileConfiguration getLangConfig() {
        return langConfig;
    }

    @Override
    public void onLoad() {
        LibraryLoaderUtil.loadDependencies(this);
    }

    @Override
    public void onEnable() {
        Instant startTime = Instant.now();

        Appender consoleAppender = new ConsoleAppender("[{loggerName}] {logLevel}: {message}");
        this.logger = new ExtendedLogger("NDailyRewards", LogLevel.INFO, List.of(consoleAppender), new ArrayList<>());

        this.injector = Guice.createInjector(
                new NDailyRewardsModule(this, logger),
                new DatabaseModule(this),
                new SchedulerSetup(this)
        );

        saveDefaultConfig();
        createLangFile();
        Language.init(this);
        new Metrics(this, 13844);

        logger.info("Loading plugin managers...");
        Logger.setGlobalLogLevel(Level.ERROR);
        try {
            this.injector.getInstance(DatabaseClient.class).open();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        this.injector.getInstance(IntegrationRegistry.class).init();
        this.injector.getInstance(RewardManager.class);
        this.injector.getInstance(MenuManager.class);

        logger.info("Registering listeners...");
        getServer().getPluginManager().registerEvents(injector.getInstance(InventoryClickListener.class), this);
        getServer().getPluginManager().registerEvents(injector.getInstance(PlayerJoinListener.class), this);

        this.commandManager = new CommandManager(this, this.injector);
        this.commandManager.registerCommands();

        Duration timeTaken = Duration.between(startTime, Instant.now());
        logger.info("Successfully enabled (took %sms)".formatted(timeTaken.toMillis()));

        if (getConfig().getBoolean("check-updates")) checkForUpdates();
    }

    @Override
    public void onDisable() {
        this.injector.getInstance(Scheduler.class).cancelTasks(this);
        this.commandManager.unregisterCommands();
        this.injector.getInstance(DatabaseClient.class).close();
        this.injector.getInstance(MenuManager.class).shutdown();
        this.injector.getInstance(RewardManager.class).unload();
    }

    public void reload() {
        reloadConfig();
        createLangFile();
        Language.init(this);
    }

    private void createLangFile() {
        langFile = new File(getDataFolder(), "lang.yml");
        if (!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            saveResource("lang.yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    private void checkForUpdates() {
        final var current = new ComparableVersion(this.getDescription().getVersion());

        supplyAsync(this.injector.getInstance(VersionFetcher.class)::fetchNewestVersion).thenApply(Objects::requireNonNull).whenComplete((newest, error) -> {
            if (error != null || newest.compareTo(current) <= 0) {
                return;
            }

            logger.warn("""
                A new version of NDailyRewards is available!
                Your version: %s
                Newest version: %s
                Download it at: %s
                """.formatted(current.toString(), newest, this.injector.getInstance(VersionFetcher.class).getDownloadUrl()));
        });
    }
}
