package space.bxteam.ndailyrewards.managers.reward;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.api.event.PlayerClaimRewardEvent;
import space.bxteam.ndailyrewards.managers.database.DatabaseManager;
import space.bxteam.ndailyrewards.managers.enums.Language;
import space.bxteam.ndailyrewards.utils.LogUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RewardManager {
    private final NDailyRewards plugin;
    private final DatabaseManager dbManager;
    private final Map<Integer, Reward> rewards = new HashMap<>();
    private final boolean resetWhenAllClaimed;
    private final int cooldown;
    private final boolean unlockAfterMidnight;

    public RewardManager(NDailyRewards plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        this.resetWhenAllClaimed = plugin.getConfig().getBoolean("rewards.reset-when-all-claimed", true);
        this.cooldown = plugin.getConfig().getInt("rewards.cooldown", 24);
        this.unlockAfterMidnight = plugin.getConfig().getBoolean("rewards.unlock-after-midnight", false);
        loadRewards();
    }

    public void unload() {
        rewards.clear();
    }

    private void loadRewards() {
        ConfigurationSection daysSection = plugin.getConfig().getConfigurationSection("rewards.days");
        if (daysSection != null) {
            for (String dayKey : daysSection.getKeys(false)) {
                int day = Integer.parseInt(dayKey);
                ConfigurationSection daySection = daysSection.getConfigurationSection(dayKey);
                if (daySection != null) {
                    List<String> lore = daySection.getStringList("lore");
                    List<String> rewardsList = daySection.getStringList("actions");
                    rewards.put(day, new Reward(lore, rewardsList));
                }
            }
        }
    }

    public void giveReward(Player player, int day) {
        UUID uuid = player.getUniqueId();
        if (!canClaimReward(uuid)) {
            player.sendMessage(Language.PREFIX.asColoredString() + Language.CLAIM_NOT_AVAILABLE.asColoredString());
            return;
        }

        Reward reward = rewards.get(day);
        if (reward != null) {
            ActionsExecutor executor = new ActionsExecutor(player, reward);
            executor.execute();

            updatePlayerRewardData(uuid, day);

            if (resetWhenAllClaimed && day >= rewards.size()) {
                resetPlayerRewardData(uuid);
            }

            PlayerClaimRewardEvent event = new PlayerClaimRewardEvent(player, day);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    private void updatePlayerRewardData(UUID uuid, int nextDay) {
        try (Connection conn = dbManager.dbSource.getConnection()) {
            String query = "UPDATE `data` SET next_time = ?, next_day = ? WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                long nextTime = getUnixTimeForNextDay();
                stmt.setLong(1, nextTime);
                stmt.setInt(2, nextDay);
                stmt.setString(3, uuid.toString());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            LogUtil.log("Could not update player reward data: " + e.getMessage(), LogUtil.LogLevel.ERROR);
            e.printStackTrace();
        }
    }

    private void resetPlayerRewardData(UUID uuid) {
        try (Connection conn = dbManager.dbSource.getConnection()) {
            String query = "UPDATE `data` SET next_time = ?, next_day = ? WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                long nextTime = getUnixTimeForNextDay();
                stmt.setLong(1, nextTime);
                stmt.setInt(2, 0);
                stmt.setString(3, uuid.toString());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            LogUtil.log("Could not reset player reward data: " + e.getMessage(), LogUtil.LogLevel.ERROR);
            e.printStackTrace();
        }
    }

    public PlayerRewardData getPlayerRewardData(UUID uuid) {
        try (Connection conn = dbManager.dbSource.getConnection()) {
            String query = "SELECT `next_time`, `next_day` FROM `data` WHERE `uuid` = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        long nextTime = rs.getLong("next_time");
                        int nextDay = rs.getInt("next_day");
                        return new PlayerRewardData(nextTime, nextDay);
                    }
                }
            }
        } catch (SQLException e) {
            LogUtil.log("Could not retrieve player reward data: " + e.getMessage(), LogUtil.LogLevel.ERROR);
            e.printStackTrace();
        }
        return new PlayerRewardData(System.currentTimeMillis(), 0);
    }

    public boolean canClaimReward(UUID uuid) {
        PlayerRewardData data = getPlayerRewardData(uuid);
        long currentTime = System.currentTimeMillis() / 1000L;
        return currentTime >= data.next() && data.currentDay() < rewards.size();
    }

    private long getUnixTimeForNextDay() {
        if (unlockAfterMidnight) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            return tomorrow.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        } else {
            return Instant.now().plusSeconds(cooldown * 3600L).getEpochSecond();
        }
    }

    public boolean shouldResetWhenAllClaimed() {
        return resetWhenAllClaimed;
    }

    public Reward getReward(int day) {
        return rewards.get(day);
    }

    public boolean isRewardClaimed(Player player, int day) {
        PlayerRewardData playerRewardData = getPlayerRewardData(player.getUniqueId());
        return playerRewardData.currentDay() >= day;
    }

    public boolean isRewardAvailable(Player player, int day) {
        PlayerRewardData playerRewardData = getPlayerRewardData(player.getUniqueId());
        return playerRewardData.currentDay() + 1 == day && System.currentTimeMillis() / 1000L >= playerRewardData.next();
    }

    public boolean isRewardNext(Player player, int day) {
        PlayerRewardData playerRewardData = getPlayerRewardData(player.getUniqueId());
        return playerRewardData.currentDay() + 1 == day && System.currentTimeMillis() / 1000L < playerRewardData.next();
    }

    public void setDay(Player player, int day) {
        UUID uuid = player.getUniqueId();
        try (Connection conn = dbManager.dbSource.getConnection()) {
            String query = "UPDATE `data` SET next_day = ? WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, day);
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            LogUtil.log("Could not set player day: " + e.getMessage(), LogUtil.LogLevel.ERROR);
            e.printStackTrace();
        }
    }
}
