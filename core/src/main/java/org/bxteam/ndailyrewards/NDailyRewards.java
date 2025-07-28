package org.bxteam.ndailyrewards;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bxteam.commons.logger.ExtendedLogger;
import org.bxteam.commons.logger.LogLevel;
import org.bxteam.commons.logger.appender.Appender;
import org.bxteam.commons.logger.appender.ConsoleAppender;
import org.bxteam.commons.logger.appender.JsonAppender;
import org.bxteam.commons.updater.MasterVersionFetcher;
import org.bxteam.commons.updater.VersionFetcher;
import org.bxteam.ndailyrewards.listeners.InventoryClickListener;
import org.bxteam.ndailyrewards.listeners.PlayerJoinListener;
import org.bxteam.ndailyrewards.hooks.HookManager;
import org.bxteam.ndailyrewards.managers.MenuManager;
import org.bxteam.ndailyrewards.database.DatabaseManager;
import org.bxteam.ndailyrewards.managers.enums.Language;
import org.bxteam.ndailyrewards.managers.reward.RewardManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public final class NDailyRewards extends JavaPlugin {
    private final VersionFetcher versionFetcher = new MasterVersionFetcher("NDailyRewards");
    private final ExtendedLogger logger;

    private static NDailyRewards instance;
    private File langFile;
    private FileConfiguration langConfig;
    private DatabaseManager database;
    private RewardManager rewardManager;
    private MenuManager menuManager;
    private LiteCommands<CommandSender> liteCommands;

    public NDailyRewards() {
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
    }

    public static NDailyRewards getInstance() {
        return NDailyRewards.instance;
    }

    public FileConfiguration getLangConfig() {
        return langConfig;
    }

    public DatabaseManager getDatabase() {
        return database;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public VersionFetcher getVersionFetcher() {
        return versionFetcher;
    }

    public ExtendedLogger getExtendedLogger() {
        return logger;
    }

    @Override
    public void onEnable() {
        Instant startTime = Instant.now();
        NDailyRewards.instance = this;
        saveDefaultConfig();
        createLangFile();
        Language.init(this);
        new Metrics(this, 13844);

        logger.info("Loading plugin managers...");
        database = new DatabaseManager();
        rewardManager = new RewardManager(this, database);
        menuManager = new MenuManager();
        new HookManager(this).registerHooks();

        logger.info("Registering listeners...");
        final Listener[] events = new Listener[]{
                new InventoryClickListener(),
                new PlayerJoinListener()
        };
        for (final Listener event : events) {
            getServer().getPluginManager().registerEvents(event, this);
        }

        this.liteCommands = LiteBukkitFactory.builder("ndailyrewards", this)
                .commands(new Commands())

                .build();

        Duration timeTaken = Duration.between(startTime, Instant.now());
        logger.info("Successfully enabled (took %sms)".formatted(timeTaken.toMillis()));

        if (getConfig().getBoolean("check-updates")) checkForUpdates();
    }

    @Override
    public void onDisable() {
        logger.info("Disabling plugin...");
        if (liteCommands != null) liteCommands.unregister();
        getServer().getScheduler().cancelTasks(this);
        database.dbSource.close();
    }

    public void reload() {
        this.database.dbSource.close();
        this.rewardManager.unload();

        reloadConfig();
        createLangFile();
        Language.init(this);
        this.database = new DatabaseManager();
        this.rewardManager = new RewardManager(this, this.database);
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

        supplyAsync(getVersionFetcher()::fetchNewestVersion).thenApply(Objects::requireNonNull).whenComplete((newest, error) -> {
            if (error != null || newest.compareTo(current) <= 0) {
                return;
            }

            logger.warn("""
                A new version of NDailyRewards is available!
                Your version: %s
                Newest version: %s
                Download it at: %s
                """.formatted(current.toString(), newest, getVersionFetcher().getDownloadUrl()));
        });
    }
}
