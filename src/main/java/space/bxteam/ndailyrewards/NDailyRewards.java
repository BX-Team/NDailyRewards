package space.bxteam.ndailyrewards;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import space.bxteam.ndailyrewards.commands.RewardCommand;
import space.bxteam.ndailyrewards.listeners.*;
import space.bxteam.ndailyrewards.managers.MenuManager;
import space.bxteam.ndailyrewards.managers.database.MySQLManager;
import space.bxteam.ndailyrewards.managers.enums.Language;
import space.bxteam.ndailyrewards.managers.reward.RewardManager;
import space.bxteam.ndailyrewards.utils.LogUtil;
import space.bxteam.ndailyrewards.utils.UpdateCheckerUtil;

import java.io.File;
import java.time.Duration;
import java.time.Instant;

public final class NDailyRewards extends JavaPlugin {
    private static NDailyRewards instance;
    private Instant startTime;
    private File langFile;
    private FileConfiguration langConfig;
    private MySQLManager database;
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
    public MySQLManager getDatabase() {
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
        startTime = Instant.now();
        NDailyRewards.instance = this;
        saveDefaultConfig();
        createLangFile();
        Language.init(this);
        registerCommands();

        LogUtil.log("Loading plugin managers...", LogUtil.LogLevel.INFO);
        database = new MySQLManager(this);
        rewardManager = new RewardManager(this, database);
        menuManager = new MenuManager();

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

        if (getConfig().getBoolean("check-updates")) {
            UpdateCheckerUtil.checkForUpdates().ifPresent(latestVersion -> {
                LogUtil.log("&aA new update is available: " + latestVersion, LogUtil.LogLevel.INFO);
                LogUtil.log("&aPlease update to the latest version to get bug fixes, security patches and new features!", LogUtil.LogLevel.INFO);
                LogUtil.log("&aDownload here: https://modrinth.com/plugin/ndailyrewards/version/" + latestVersion, LogUtil.LogLevel.INFO);
            });
        }
    }

    @Override
    public void onDisable() {
        LogUtil.log("Disabling plugin...", LogUtil.LogLevel.INFO);
        database.dbSource.close();
    }

    public void reload() {
        database.dbSource.close();
        rewardManager.unload();

        reloadConfig();
        createLangFile();
        Language.init(this);
        database = new MySQLManager(this);
        rewardManager = new RewardManager(this, database);
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
}
