package org.bxteam.ndailyrewards.command;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.configuration.Language;
import org.bxteam.ndailyrewards.manager.reward.RewardManager;
import org.bxteam.ndailyrewards.messaging.MessageService;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"reward", "rw", "ndailyrewards", "ndr"})
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SetDayCommand {
    private final NDailyRewards plugin;
    private final RewardManager rewardManager;
    private final MessageService messageService;

    @Subcommand("setday")
    @CommandPermission("ndailyrewards.setday")
    void setDay(BukkitCommandActor sender, Player target, Integer day) {
        int maxDays = getMaxDays();

        if (day < 1 || day > maxDays) {
            messageService.send(sender.sender(), Language.INVALID_DAY,
                    "<max-day>", String.valueOf(maxDays));
            return;
        }

        try {
            this.rewardManager.setDay(target, day - 1);
            messageService.send(sender.sender(), Language.COMMANDS_SETDAY,
                    "<player>", target.getName(),
                    "<day>", String.valueOf(day));
        } catch (NumberFormatException e) {
            messageService.send(sender.sender(), Language.INVALID_SYNTAX);
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
