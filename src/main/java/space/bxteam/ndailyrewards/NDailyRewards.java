package space.bxteam.ndailyrewards;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import space.bxteam.ndailyrewards.api.github.*;
import space.bxteam.ndailyrewards.commands.RewardCommand;
import space.bxteam.ndailyrewards.hooks.HookManager;
import space.bxteam.ndailyrewards.listeners.*;
import space.bxteam.ndailyrewards.managers.MenuManager;
import space.bxteam.ndailyrewards.managers.database.DatabaseManager;
import space.bxteam.ndailyrewards.managers.enums.Language;
import space.bxteam.ndailyrewards.managers.reward.RewardManager;
import space.bxteam.ndailyrewards.utils.LogUtil;
import space.bxteam.ndailyrewards.utils.metrics.Metrics;
import space.bxteam.ndailyrewards.utils.DelayedTask;

import java.io.File;
import java.time.Duration;
import java.time.Instant;

public final class NDailyRewards extends JavaPlugin {
    private static NDailyRewards instance;
    private File langFile;
    private FileConfiguration langConfig;
    private DatabaseManager database;
    private RewardManager rewardManager;
    private MenuManager menuManager;

    /**
     * Get instance of plugin
     *
     * @return NDR instance
     */
    public static NDailyRewards getInstance() {
        return NDailyRewards.instance;
    }

    /**
     * Get plugin language manager
     *
     * @return language config
     */
    public FileConfiguration getLangConfig() {
        return langConfig;
    }

    /**
     * Get plugin database manager
     *
     * @return database manager
     */
    public DatabaseManager getDatabase() {
        return database;
    }

    /**
     * Get plugin reward manager
     *
     * @return reward manager
     */
    public RewardManager getRewardManager() {
        return rewardManager;
    }

    /**
     * Get plugin menu manager
     *
     * @return menu manager
     */
    public MenuManager getMenuManager() {
        return menuManager;
    }

    @Override
    public void onEnable() {
        Instant startTime = Instant.now();
        NDailyRewards.instance = this;
        saveDefaultConfig();
        createLangFile();
        Language.init(this);
        registerCommands();
        new Metrics(this, 13844);

        LogUtil.log("Loading plugin managers...", LogUtil.LogLevel.INFO);
        database = new DatabaseManager();
        rewardManager = new RewardManager(this, database);
        menuManager = new MenuManager();
        new HookManager(this).registerHooks();
        new DelayedTask(this);

        LogUtil.log("Registering listeners...", LogUtil.LogLevel.INFO);
        final Listener[] events = new Listener[]{
                new InventoryClickListener(),
                new PlayerJoinListener()
        };
        for (final Listener event : events) {
            getServer().getPluginManager().registerEvents(event, this);
        }

        Duration timeTaken = Duration.between(startTime, Instant.now());
        LogUtil.log("Successfully enabled (took " + timeTaken.toMillis() + "ms)", LogUtil.LogLevel.INFO);

        if (getConfig().getBoolean("check-updates")) checkForUpdates();
    }

    @Override
    public void onDisable() {
        LogUtil.log("Disabling plugin...", LogUtil.LogLevel.INFO);
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

    private void registerCommands() {
        new RewardCommand().registerMainCommand(this, "reward");
    }

    private void checkForUpdates() {
        GitCheck gitCheck = new GitCheck();
        GitRepository repository = GitRepository.of("BX-Team", "NDailyRewards");

        GitCheckResult result = gitCheck.checkRelease(repository, GitTag.of("v" + getDescription().getVersion()));
        if (!result.isUpToDate()) {
            GitRelease release = result.getLatestRelease();
            GitTag tag = release.getTag();

            LogUtil.log("&aA new update is available: &e" + tag.getTag(), LogUtil.LogLevel.INFO);
            LogUtil.log("&aDownload here: &e" + release.getPageUrl(), LogUtil.LogLevel.INFO);
        }
    }
}
