package space.bxteam.ndailyrewards;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import space.bxteam.ndailyrewards.configuration.ConfigReader;
import space.bxteam.ndailyrewards.configuration.Language;
import space.bxteam.ndailyrewards.managers.database.MySQLManager;

import java.io.File;

import static space.bxteam.ndailyrewards.configuration.Language.getLangFile;

public final class NDailyRewards extends JavaPlugin {
    public static NDailyRewards instance;
    public final @NotNull ConfigReader config = new ConfigReader(getConfig());
    public File langFile;
    public Language language;
    public MySQLManager database;

    /**
     * Get instance of plugin
     *
     * @return NDR instance
     */
    public static NDailyRewards getInstance() {
        return NDailyRewards.instance;
    }

    /**
     * Get plugin config
     *
     * @return plugin config
     */
    public ConfigReader config() {
        return config;
    }

    /**
     * Get plugin language manager
     *
     * @return language class
     */
    public Language getLanguage() {
        return language;
    }

    @Override
    public void onEnable() {
        NDailyRewards.instance = this;
        saveDefaultConfig();
        Language.saveLanguages();
        this.langFile = getLangFile();
        language = new Language(langFile);

        database = new MySQLManager(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
