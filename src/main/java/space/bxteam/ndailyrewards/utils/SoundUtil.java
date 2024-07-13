package space.bxteam.ndailyrewards.utils;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.bxteam.ndailyrewards.NDailyRewards;

public class SoundUtil {
    private static String[] getSound(String command) {
        if (!NDailyRewards.getInstance().getConfig().getBoolean("sound." + command + ".enable")) return null;

        String sound = NDailyRewards.getInstance().getConfig().getString("sound." + command + ".type");

        return sound.split(":");
    }

    public static void playSound(@Nullable Player sender, @Nullable Player recipient, @NotNull String command) {
        if (recipient == null) return;

        String[] params = getSound(command);
        if (params == null) return;

        try {
            recipient.playSound(recipient.getLocation(), Sound.valueOf(params[0]), Float.parseFloat(params[1]), Float.parseFloat(params[2]));
        } catch (IllegalArgumentException exception) {
            LogUtil.log("Incorrect sound " + params[0] + " for " + command + ".sound.type", LogUtil.LogLevel.ERROR);
            exception.printStackTrace();
        }
    }

    public static void playSound(@Nullable Player sender, @Nullable Location location, @NotNull String command) {
        if (location == null || location.getWorld() == null) return;

        String[] params = getSound(command);
        if (params == null) return;

        try {
            location.getWorld().playSound(location, Sound.valueOf(params[0]), Float.parseFloat(params[1]), Float.parseFloat(params[2]));
        } catch (IllegalArgumentException exception) {
            LogUtil.log("Incorrect sound " + params[0] + " for " + command + ".sound.type", LogUtil.LogLevel.ERROR);
            exception.printStackTrace();
        }
    }

    public static void playSound(@Nullable Player player, @NotNull String command) {
        playSound(player, player, command);
    }
}
