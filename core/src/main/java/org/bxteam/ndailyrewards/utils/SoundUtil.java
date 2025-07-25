package org.bxteam.ndailyrewards.utils;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bxteam.ndailyrewards.NDailyRewards;

public class SoundUtil {
    private static String[] getSound(String action) {
        if (!NDailyRewards.getInstance().getConfig().getBoolean("sound." + action + ".enabled")) return null;

        String sound = NDailyRewards.getInstance().getConfig().getString("sound." + action + ".type");

        return sound.split(":");
    }

    public static void playSound(@Nullable Player sender, @Nullable Player recipient, @NotNull String action) {
        if (recipient == null) return;

        String[] params = getSound(action);
        if (params == null) return;

        try {
            recipient.playSound(recipient.getLocation(), Sound.valueOf(params[0]), Float.parseFloat(params[1]), Float.parseFloat(params[2]));
        } catch (IllegalArgumentException exception) {
            NDailyRewards.getInstance().getExtendedLogger().error("Incorrect sound %s for sound.%s.type".formatted(params[0], action));
            exception.printStackTrace();
        }
    }

    public static void playSound(@Nullable Player sender, @Nullable Location location, @NotNull String action) {
        if (location == null || location.getWorld() == null) return;

        String[] params = getSound(action);
        if (params == null) return;

        try {
            location.getWorld().playSound(location, Sound.valueOf(params[0]), Float.parseFloat(params[1]), Float.parseFloat(params[2]));
        } catch (IllegalArgumentException exception) {
            NDailyRewards.getInstance().getExtendedLogger().error("Incorrect sound %s for sound.%s.type".formatted(params[0], action));
            exception.printStackTrace();
        }
    }

    public static void playSound(@Nullable Player player, @NotNull String action) {
        playSound(player, player, action);
    }
}
