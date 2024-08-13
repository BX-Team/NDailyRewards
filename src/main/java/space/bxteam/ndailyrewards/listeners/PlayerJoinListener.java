package space.bxteam.ndailyrewards.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.api.event.AutoClaimEvent;
import space.bxteam.ndailyrewards.api.event.PlayerReceiveReminderEvent;
import space.bxteam.ndailyrewards.api.github.*;
import space.bxteam.ndailyrewards.managers.enums.Language;
import space.bxteam.ndailyrewards.utils.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        try (Connection conn = NDailyRewards.getInstance().getDatabase().dbSource.getConnection()) {
            String checkQuery = "SELECT COUNT(*) FROM `data` WHERE uuid = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, uuid.toString());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return;
                    }
                }
            }

            String insertQuery = "INSERT INTO `data` (uuid, next_time, next_day) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                long nextTime = getUnixTimeForNextDay();
                insertStmt.setString(1, uuid.toString());
                insertStmt.setLong(2, nextTime);
                insertStmt.setInt(3, 0);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            LogUtil.log("Could not create initial player data: " + e.getMessage(), LogUtil.LogLevel.ERROR);
            e.printStackTrace();
        }
    }

    private long getUnixTimeForNextDay() {
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
        UUID uuid = player.getUniqueId();

        if (NDailyRewards.getInstance().getConfig().getBoolean("events.auto-claim-reward")) {
            int currentDay = NDailyRewards.getInstance().getRewardManager().getPlayerRewardData(player.getUniqueId()).currentDay() + 1;
            if (NDailyRewards.getInstance().getRewardManager().isRewardAvailable(player, currentDay)) {
                NDailyRewards.getInstance().getRewardManager().giveReward(player, currentDay);
                AutoClaimEvent autoClaimEvent = new AutoClaimEvent(player, currentDay);
                Bukkit.getPluginManager().callEvent(autoClaimEvent);
            }
        }

        if (NDailyRewards.getInstance().getConfig().getBoolean("events.open-gui-when-available")) {
            int currentDay = NDailyRewards.getInstance().getRewardManager().getPlayerRewardData(player.getUniqueId()).currentDay() + 1;
            if (NDailyRewards.getInstance().getRewardManager().isRewardAvailable(player, currentDay)) {
                NDailyRewards.getInstance().getMenuManager().openRewardsMenu(player);
                if (NDailyRewards.getInstance().getConfig().getBoolean("sound.open.enabled")) {
                    SoundUtil.playSound(player, "open");
                }
            }
        }

        if (NDailyRewards.getInstance().getConfig().getBoolean("events.notify-when-available")) {
            int currentDay = NDailyRewards.getInstance().getRewardManager().getPlayerRewardData(player.getUniqueId()).currentDay() + 1;
            if (NDailyRewards.getInstance().getRewardManager().isRewardAvailable(player, currentDay)) {
                player.sendMessage(Language.PREFIX.asColoredString() + Language.EVENTS_NOTIFY_WHEN_AVAILABLE.asColoredString());
                PlayerReceiveReminderEvent reminderEvent = new PlayerReceiveReminderEvent(player, NDailyRewards.getInstance().getRewardManager().getPlayerRewardData(uuid).currentDay() + 1);
                Bukkit.getPluginManager().callEvent(reminderEvent);
            }
        }
    }

    @EventHandler
    public void updateChecker(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (NDailyRewards.getInstance().getConfig().getBoolean("check-updates") && player.hasPermission(Permissions.UPDATE_NOTIFY)) {
            GitCheck gitCheck = new GitCheck();
            GitRepository repository = GitRepository.of("BX-Team", "NDailyRewards");

            GitCheckResult result = gitCheck.checkRelease(repository, GitTag.of(NDailyRewards.getInstance().getDescription().getVersion()));
            if (!result.isUpToDate()) {
                GitRelease release = result.getLatestRelease();
                GitTag tag = release.getTag();

                player.sendMessage(TextUtils.applyColor(Language.PREFIX.asString() + "&aA new update is available: &e" + tag.getTag()));
                player.sendMessage(TextUtils.applyColor(Language.PREFIX.asString() + "&aDownload here: &e" + release.getPageUrl()));
            }
        }
    }
}
