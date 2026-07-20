package org.bxteam.ndailyrewards.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bxteam.helix.logger.ExtendedLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
public class SoundUtil {
    private final Plugin plugin;
    private final ExtendedLogger logger;

    @Inject
    public SoundUtil(Plugin plugin, ExtendedLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    private static final float DEFAULT_VOLUME = 1.0f;
    private static final float DEFAULT_PITCH = 1.0f;

    private ParsedSound getSound(String action) {
        if (!plugin.getConfig().getBoolean("sound." + action + ".enabled")) return null;

        String sound = plugin.getConfig().getString("sound." + action + ".type");
        if (sound == null || sound.isBlank()) {
            logger.error("Missing sound.%s.type in config".formatted(action));
            return null;
        }

        String[] params = sound.split(":");

        Sound parsedSound;
        try {
            parsedSound = Sound.valueOf(params[0].trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            logger.error("Incorrect sound '%s' for sound.%s.type".formatted(params[0], action));
            return null;
        }

        float volume = parseFloatOrDefault(params, 1, DEFAULT_VOLUME, action);
        float pitch = parseFloatOrDefault(params, 2, DEFAULT_PITCH, action);

        return new ParsedSound(parsedSound, volume, pitch);
    }

    private float parseFloatOrDefault(String[] params, int index, float fallback, String action) {
        if (index >= params.length || params[index].isBlank()) return fallback;
        try {
            return Float.parseFloat(params[index].trim());
        } catch (NumberFormatException exception) {
            logger.warn("Invalid number '%s' in sound.%s.type — using default %s".formatted(params[index], action, fallback));
            return fallback;
        }
    }

    public void playSound(@Nullable Player sender, @Nullable Player recipient, @NotNull String action) {
        if (recipient == null) return;

        ParsedSound sound = getSound(action);
        if (sound == null) return;

        recipient.playSound(recipient.getLocation(), sound.sound(), sound.volume(), sound.pitch());
    }

    public void playSound(@Nullable Player sender, @Nullable Location location, @NotNull String action) {
        if (location == null || location.getWorld() == null) return;

        ParsedSound sound = getSound(action);
        if (sound == null) return;

        location.getWorld().playSound(location, sound.sound(), sound.volume(), sound.pitch());
    }

    private record ParsedSound(Sound sound, float volume, float pitch) {}

    public void playSound(@Nullable Player player, @NotNull String action) {
        playSound(player, player, action);
    }
}
