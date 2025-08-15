package org.bxteam.ndailyrewards.command;

import org.bukkit.command.CommandSender;
import org.bxteam.ndailyrewards.configuration.Language;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"reward", "rw", "ndailyrewards", "ndr"})
public class HelpCommand {
    @Subcommand("help")
    @CommandPermission("ndailyrewards.help")
    void help(CommandSender sender) {
        for (String message : Language.COMMANDS_HELP.asColoredStringList()) {
            sender.sendMessage(message);
        }
    }
}
