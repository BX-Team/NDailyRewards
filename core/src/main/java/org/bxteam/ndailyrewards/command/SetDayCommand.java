package org.bxteam.ndailyrewards.command;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.configuration.Language;
import org.bxteam.ndailyrewards.manager.reward.RewardManager;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"reward", "rw", "ndailyrewards", "ndr"})
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SetDayCommand {
    private final NDailyRewards plugin;
    private final RewardManager rewardManager;

    @Subcommand("setday")
    @CommandPermission("ndailyrewards.setday")
    void setDay(BukkitCommandActor sender, Player target, Integer day) {
        int maxDays = getMaxDays();

        if (day < 1 || day > maxDays) {
            sender.sender().sendMessage(Language.PREFIX.asColoredString() + Language.INVALID_DAY.asColoredString()
                    .replace("<max-day>", String.valueOf(maxDays)));
            return;
        }

        try {
            this.rewardManager.setDay(target, day - 1);
            sender.sender().sendMessage(Language.PREFIX.asColoredString() + Language.COMMANDS_SETDAY.asColoredString()
                    .replace("<player>", target.getName())
                    .replace("<day>", String.valueOf(day)));
        } catch (NumberFormatException e) {
            sender.sender().sendMessage(Language.PREFIX.asColoredString() + Language.INVALID_SYNTAX.asColoredString());
        }
    }

    private int getMaxDays() {
        ConfigurationSection daysSection = this.plugin.getConfig().getConfigurationSection("rewards.days");
        if (daysSection == null) {
            return 0;
        }

        int maxDay = 0;
        for (String key : daysSection.getKeys(false)) {
            try {
                int day = Integer.parseInt(key);
                if (day > maxDay) {
                    maxDay = day;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return maxDay;
    }
}
