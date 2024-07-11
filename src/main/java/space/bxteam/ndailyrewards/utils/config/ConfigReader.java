package space.bxteam.ndailyrewards.utils.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class ConfigReader {
    public @NotNull FileConfiguration config;

    public ConfigReader(@NotNull final FileConfiguration config) {
        this.config = config;
    }
}
