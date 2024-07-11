package space.bxteam.ndailyrewards;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import space.bxteam.ndailyrewards.utils.config.ConfigReader;

public final class NDailyRewards extends JavaPlugin {
    public static NDailyRewards instance;
    private final @NotNull ConfigReader config = new ConfigReader(getConfig());

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
    public @NotNull ConfigReader config() {
        return config;
    }

    @Override
    public void onEnable() {
        NDailyRewards.instance = this;
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
