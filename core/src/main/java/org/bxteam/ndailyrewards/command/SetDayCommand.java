package org.bxteam.ndailyrewards.command;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bxteam.ndailyrewards.configuration.Messages;
import org.bxteam.ndailyrewards.configuration.PluginConfig;
import org.bxteam.ndailyrewards.manager.reward.RewardManager;
import org.bxteam.ndailyrewards.messaging.MessageService;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"reward", "rw", "ndailyrewards", "ndr"})
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SetDayCommand {
    private final PluginConfig config;
    private final RewardManager rewardManager;
    private final MessageService messageService;
    private final Messages messages;

    @Subcommand("setday")
    @CommandPermission("ndailyrewards.setday")
    void setDay(BukkitCommandActor sender, Player target, Integer day) {
        int maxDays = this.config.rewards.days.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);

        if (day < 1 || day > maxDays) {
            messageService.send(sender.sender(), messages.invalidDay,
                    "<max-day>", String.valueOf(maxDays));
            return;
        }

        this.rewardManager.setDay(target, day - 1);
        messageService.send(sender.sender(), messages.commands.setday,
                "<player>", target.getName(),
                "<day>", String.valueOf(day));
    }
}
