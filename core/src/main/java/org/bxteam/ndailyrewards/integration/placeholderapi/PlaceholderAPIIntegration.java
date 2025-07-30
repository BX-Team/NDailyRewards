package org.bxteam.ndailyrewards.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bxteam.ndailyrewards.integration.Integration;
import org.bxteam.ndailyrewards.manager.reward.PlayerRewardData;
import org.bxteam.ndailyrewards.manager.reward.RewardManager;
import org.jetbrains.annotations.NotNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlaceholderAPIIntegration extends PlaceholderExpansion implements Integration {
    private final RewardManager rewardManager;
    private final PluginDescriptionFile pluginDescription;

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
        if (params.equalsIgnoreCase("reward_day")) {
            PlayerRewardData playerRewardData = this.rewardManager.getPlayerRewardData(player.getUniqueId());

            return String.valueOf(playerRewardData.currentDay() + 1);
        }

        if (params.equalsIgnoreCase("remaining_time")) {
            PlayerRewardData playerRewardData = this.rewardManager.getPlayerRewardData(player.getUniqueId());

            long timeLeft = playerRewardData.next() - System.currentTimeMillis() / 1000L;
            if (timeLeft < 0) {
                return "00:00:00";
            } else {
                return formatTime(timeLeft);
            }
        }

        return null;
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
