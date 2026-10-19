package org.bxteam.ndailyrewards.command;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bxteam.ndailyrewards.configuration.Messages;
import org.bxteam.ndailyrewards.messaging.MessageService;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"reward", "rw", "ndailyrewards", "ndr"})
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HelpCommand {
    private final MessageService messageService;
    private final Messages messages;

    @Subcommand("help")
    @CommandPermission("ndailyrewards.help")
    void help(CommandSender sender) {
        messageService.sendList(sender, messages.commands.help);
    }
}
