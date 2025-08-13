package org.bxteam.ndailyrewards.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bxteam.commons.logger.ExtendedLogger;

@Singleton
public class SoundUtil {
    private final Plugin plugin;
    private final ExtendedLogger logger;

    @Inject
    public SoundUtil(Plugin plugin, ExtendedLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    private String[] getSound(String action) {
        if (!plugin.getConfig().getBoolean("sound." + action + ".enabled")) return null;

        String sound = plugin.getConfig().getString("sound." + action + ".type");

        return sound.split(":");
    }

    public void playSound(@Nullable Player sender, @Nullable Player recipient, @NotNull String action) {
        if (recipient == null) return;

        String[] params = getSound(action);
        if (params == null) return;

        try {
            recipient.playSound(recipient.getLocation(), Sound.valueOf(params[0]), Float.parseFloat(params[1]), Float.parseFloat(params[2]));
        } catch (IllegalArgumentException exception) {
            logger.error("Incorrect sound %s for sound.%s.type".formatted(params[0], action));
        }
    }

    public void playSound(@Nullable Player sender, @Nullable Location location, @NotNull String action) {
        if (location == null || location.getWorld() == null) return;

        String[] params = getSound(action);
        if (params == null) return;

        try {
            location.getWorld().playSound(location, Sound.valueOf(params[0]), Float.parseFloat(params[1]), Float.parseFloat(params[2]));
        } catch (IllegalArgumentException exception) {
            logger.error("Incorrect sound %s for sound.%s.type".formatted(params[0], action));
        }
    }

    public void playSound(@Nullable Player player, @NotNull String action) {
        playSound(player, player, action);
    }
}
