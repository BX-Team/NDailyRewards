package org.bxteam.ndailyrewards.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bxteam.helix.logger.ExtendedLogger;
import org.bxteam.ndailyrewards.integration.Integration;
import org.bxteam.ndailyrewards.manager.reward.PlayerRewardData;
import org.bxteam.ndailyrewards.manager.reward.RewardManager;
import org.jetbrains.annotations.NotNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlaceholderAPIIntegration extends PlaceholderExpansion implements Integration {
    private final RewardManager rewardManager;
    private final PluginDescriptionFile pluginDescription;
    private final ExtendedLogger logger;

    @Override
    public void enable() {
        this.register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "dailyrewards";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", pluginDescription.getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return this.pluginDescription.getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        if (rewardManager.isDebugEnabled()) logger.info("[DEBUG-PAPI] Processing placeholder: " + params + " for player: " + player.getName());

        PlayerRewardData data = this.rewardManager.getPlayerRewardData(player.getUniqueId());
        if (data == null) {
            return switch (params.toLowerCase()) {
                case "has_claimed_today" -> "false";
                case "remaining_time" -> "00:00:00";
                default -> "0";
            };
        }

        long now = System.currentTimeMillis() / 1000L;

        return switch (params.toLowerCase()) {
            case "reward_day", "next_day" -> String.valueOf(data.currentDay() + 1);
            case "streak" -> String.valueOf(data.currentDay());
            case "max_streak" -> String.valueOf(data.maxStreak());
            case "missed_days" -> String.valueOf(rewardManager.computeMissedDaysSince(data, now));
            case "missedtotal_days" -> String.valueOf(data.missedTotal());
            case "has_claimed_today" -> String.valueOf(rewardManager.hasClaimedToday(data));
            case "remaining_time" -> {
                long timeLeft = data.next() - now;
                yield timeLeft < 0 ? "00:00:00" : formatTime(timeLeft);
            }
            default -> null;
        };
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
