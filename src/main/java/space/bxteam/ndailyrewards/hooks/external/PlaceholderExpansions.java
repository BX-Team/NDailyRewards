package space.bxteam.ndailyrewards.hooks.external;

import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.manager.objects.DUser;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlaceholderExpansions extends me.clip.placeholderapi.expansion.PlaceholderExpansion {

    @Override
    public @NotNull String getAuthor() {
        return NDailyRewards.getInstance().getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ndr";
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

    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        if (identifier.equalsIgnoreCase("next_reward_time")) {
            String unix = String.valueOf(new DUser(Objects.requireNonNull(player.getPlayer())).getTimeToGetReward());
            String format = this.getString("date_format", "dd/MM/yyyy hh:mma");

            return new java.text.SimpleDateFormat(format).format(new java.util.Date(Long.parseLong(unix)));
        }

        if (identifier.equalsIgnoreCase("warmup_reward_time")) {
            String unix = String.valueOf(new DUser(Objects.requireNonNull(player.getPlayer())).remainingLoginDuration());
            String format = this.getString("date_format", "dd/MM/yyyy hh:mma");

            return new java.text.SimpleDateFormat(format).format(new java.util.Date(Long.parseLong(unix)));
        }

        return null;
    }
}
