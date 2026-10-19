package org.bxteam.ndailyrewards.configuration;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * A sound written as {@code SOUND_NAME[:volume[:pitch]]}, the format of {@code sound.open.type} and of the
 * {@code [sound]} action.
 *
 * @param name the sound name as written, kept because {@code Sound#name()} does not exist on 1.21.3+ servers
 */
public record SoundEffect(String name, Sound sound, float volume, float pitch) {
    public static SoundEffect parse(String value) {
        String[] parts = value.trim().split(":");
        String name = parts[0].trim().toUpperCase(Locale.ROOT);
        Sound sound;
        try {
            sound = Sound.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unknown sound '" + parts[0].trim() + "', expected a name such as ENTITY_EXPERIENCE_ORB_PICKUP");
        }
        return new SoundEffect(name, sound, number(parts, 1, value), number(parts, 2, value));
    }

    private static float number(String[] parts, int index, String value) {
        if (index >= parts.length || parts[index].isBlank()) {
            return 1.0f;
        }
        try {
            return Float.parseFloat(parts[index].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + parts[index].trim() + "' in '" + value + "' is not a number, expected sound:volume:pitch");
        }
    }

    public void play(Player player) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    @Override
    public String toString() {
        return name + ":" + format(volume) + ":" + format(pitch);
    }

    private static String format(float number) {
        return number == (int) number ? String.valueOf((int) number) : String.valueOf(number);
    }
}
