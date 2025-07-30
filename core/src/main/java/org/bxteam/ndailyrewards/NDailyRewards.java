package org.bxteam.ndailyrewards;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.Logger;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bxteam.commons.logger.ExtendedLogger;
import org.bxteam.commons.logger.LogLevel;
import org.bxteam.commons.logger.appender.Appender;
import org.bxteam.commons.logger.appender.ConsoleAppender;
import org.bxteam.commons.logger.appender.JsonAppender;
import org.bxteam.commons.scheduler.Scheduler;
import org.bxteam.commons.updater.VersionFetcher;
import org.bxteam.ndailyrewards.database.DatabaseClient;
import org.bxteam.ndailyrewards.database.DatabaseModule;
import org.bxteam.ndailyrewards.integration.IntegrationRegistry;
import org.bxteam.ndailyrewards.listener.InventoryClickListener;
import org.bxteam.ndailyrewards.listener.PlayerJoinListener;
import org.bxteam.ndailyrewards.manager.menu.MenuManager;
import org.bxteam.ndailyrewards.configuration.Language;
import org.bxteam.ndailyrewards.manager.reward.RewardManager;
import org.bxteam.ndailyrewards.scheduler.SchedulerSetup;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public final class NDailyRewards extends JavaPlugin {
    private Injector injector;
    private ExtendedLogger logger;
    private File langFile;
    private FileConfiguration langConfig;
    private LiteCommands<CommandSender> liteCommands;

    public FileConfiguration getLangConfig() {
        return langConfig;
    }

    @Override
    public void onEnable() {
        Instant startTime = Instant.now();

        Appender consoleAppender = new ConsoleAppender("[{loggerName}] {logLevel}: {message}");
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
        File logsFile = new File("plugins/NDailyRewards/logs/ndr-logs-" + date + ".txt");
        if (!logsFile.exists()) {
            try {
                logsFile.getParentFile().mkdirs();
                logsFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        JsonAppender jsonAppender = new JsonAppender(false, false, true, logsFile.getPath());
        this.logger = new ExtendedLogger("NDailyRewards", LogLevel.INFO, List.of(consoleAppender, jsonAppender), new ArrayList<>());

        this.injector = Guice.createInjector(
                new NDailyRewardsModule(this, logger),
                new DatabaseModule(this),
                new SchedulerSetup(this)
        );

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

        saveDefaultConfig();
        createLangFile();
        Language.init(this);
        new Metrics(this, 13844);

        logger.info("Registering listeners...");
        getServer().getPluginManager().registerEvents(injector.getInstance(InventoryClickListener.class), this);
        getServer().getPluginManager().registerEvents(injector.getInstance(PlayerJoinListener.class), this);

        this.liteCommands = LiteBukkitFactory.builder("ndailyrewards", this)
                .commands(this.injector.getInstance(Commands.class))

                .build();

        Duration timeTaken = Duration.between(startTime, Instant.now());
        logger.info("Successfully enabled (took %sms)".formatted(timeTaken.toMillis()));

        if (getConfig().getBoolean("check-updates")) checkForUpdates();
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        this.injector.getInstance(Scheduler.class).cancelTasks(this);
        if (liteCommands != null) liteCommands.unregister();
        this.injector.getInstance(DatabaseClient.class).close();
        this.injector.getInstance(MenuManager.class).shutdown();
        this.injector.getInstance(RewardManager.class).unload();
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
