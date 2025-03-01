package org.bxteam.ndailyrewards.listeners;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bxteam.ndailyrewards.utils.Permissions;
import org.bxteam.ndailyrewards.utils.SoundUtil;
import org.bxteam.ndailyrewards.utils.TextUtils;
import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.api.event.AutoClaimEvent;
import org.bxteam.ndailyrewards.api.event.PlayerReceiveReminderEvent;
import org.bxteam.ndailyrewards.managers.enums.Language;
import org.bxteam.ndailyrewards.managers.reward.PlayerRewardData;
import org.bxteam.ndailyrewards.managers.reward.RewardManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        boolean isFirstJoin = false;

        try (Connection conn = NDailyRewards.getInstance().getDatabase().dbSource.getConnection()) {
            String checkQuery = "SELECT COUNT(*) FROM `data` WHERE uuid = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, uuid.toString());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        isFirstJoin = true;
                    } else {
                        return;
                    }
                }
            }

            String insertQuery = "INSERT INTO `data` (uuid, next_time, next_day) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                long nextTime = getUnixTimeForNextDay(isFirstJoin);
                insertStmt.setString(1, uuid.toString());
                insertStmt.setLong(2, nextTime);
                insertStmt.setInt(3, 0);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            NDailyRewards.getInstance().getExtendedLogger().error("Could not create initial player data: %s".formatted(e.getMessage()));
            e.printStackTrace();
        }
    }

    private long getUnixTimeForNextDay(boolean isFirstJoin) {
        if (isFirstJoin && NDailyRewards.getInstance().getConfig().getBoolean("rewards.first-join-reward")) {
            return Instant.now().getEpochSecond();
        }

        if (NDailyRewards.getInstance().getConfig().getBoolean("rewards.unlock-after-midnight")) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            return tomorrow.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        } else {
            return Instant.now().plusSeconds(NDailyRewards.getInstance().getConfig().getInt("rewards.cooldown") * 3600L).getEpochSecond();
        }
    }

    @EventHandler
    public void onPlayerJoinEvents(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        RewardManager rewardManager = NDailyRewards.getInstance().getRewardManager();
        PlayerRewardData playerRewardData = rewardManager.getPlayerRewardData(player.getUniqueId());
        int currentDay = playerRewardData.currentDay() + 1;
        long delayTime = NDailyRewards.getInstance().getConfig().getLong("events.auto-claim-delay");

        if (NDailyRewards.getInstance().getConfig().getBoolean("events.auto-claim-reward") && rewardManager.isRewardAvailable(playerRewardData, currentDay)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    rewardManager.giveReward(player, currentDay);
                    Bukkit.getPluginManager().callEvent(new AutoClaimEvent(player, currentDay));
                }
            }.runTaskLater(NDailyRewards.getInstance(), delayTime * 20L);
        }

        if (NDailyRewards.getInstance().getConfig().getBoolean("events.open-gui-when-available") && rewardManager.isRewardAvailable(playerRewardData, currentDay)) {
            NDailyRewards.getInstance().getMenuManager().openRewardsMenu(player);
            if (NDailyRewards.getInstance().getConfig().getBoolean("sound.open.enabled")) {
                SoundUtil.playSound(player, "open");
            }
        }

        if (NDailyRewards.getInstance().getConfig().getBoolean("events.notify-when-available") && rewardManager.isRewardAvailable(playerRewardData, currentDay)) {
            player.sendMessage(Language.PREFIX.asColoredString() + Language.EVENTS_NOTIFY_WHEN_AVAILABLE.asColoredString());
            Bukkit.getPluginManager().callEvent(new PlayerReceiveReminderEvent(player, currentDay));
        }
    }

    @EventHandler
    public void updateChecker(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (NDailyRewards.getInstance().getConfig().getBoolean("check-updates") && player.hasPermission(Permissions.UPDATE_NOTIFY)) {
            final var current = new ComparableVersion(NDailyRewards.getInstance().getDescription().getVersion());

            supplyAsync(NDailyRewards.getInstance().getVersionFetcher()::fetchNewestVersion).thenApply(Objects::requireNonNull).whenComplete((newest, error) -> {
                if (error != null || newest.compareTo(current) <= 0) {
                    return;
                }

                player.sendMessage(TextUtils.applyColor(Language.PREFIX.asString() + "&aA new update is available: &e" + newest));
                player.sendMessage(TextUtils.applyColor(Language.PREFIX.asString() + "&aDownload here: &e" + NDailyRewards.getInstance().getVersionFetcher().getDownloadUrl()));
            });
        }
    }
}
