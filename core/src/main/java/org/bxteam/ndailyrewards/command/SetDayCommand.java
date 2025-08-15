package org.bxteam.ndailyrewards.command;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bxteam.ndailyrewards.configuration.Language;
import org.bxteam.ndailyrewards.manager.reward.RewardManager;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"reward", "rw", "ndailyrewards", "ndr"})
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SetDayCommand {
    private final RewardManager rewardManager;

    @Subcommand("setday")
    @CommandPermission("ndailyrewards.setday")
    void setDay(BukkitCommandActor sender, Player target, Integer day) {
        try {
            this.rewardManager.setDay(target, day - 1);
            sender.sender().sendMessage(Language.PREFIX.asColoredString() + Language.COMMANDS_SETDAY.asColoredString()
                    .replace("<player>", target.getName())
                    .replace("<day>", String.valueOf(day)));
        } catch (NumberFormatException e) {
            sender.sender().sendMessage(Language.PREFIX.asColoredString() + Language.INVALID_SYNTAX.asColoredString());
        }
    }
}
