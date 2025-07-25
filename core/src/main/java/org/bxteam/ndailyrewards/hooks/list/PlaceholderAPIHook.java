package org.bxteam.ndailyrewards.hooks.list;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.managers.reward.PlayerRewardData;
import org.bxteam.ndailyrewards.managers.reward.RewardManager;

public class PlaceholderAPIHook extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "dailyrewards";
    }

    @Override
    public @NotNull String getAuthor() {
        return NDailyRewards.getInstance().getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return NDailyRewards.getInstance().getDescription().getVersion();
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
            RewardManager rewardManager = NDailyRewards.getInstance().getRewardManager();
            PlayerRewardData playerRewardData = rewardManager.getPlayerRewardData(player.getUniqueId());

            return String.valueOf(playerRewardData.currentDay() + 1);
        }

        if (params.equalsIgnoreCase("remaining_time")) {
            RewardManager rewardManager = NDailyRewards.getInstance().getRewardManager();
            PlayerRewardData playerRewardData = rewardManager.getPlayerRewardData(player.getUniqueId());

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
