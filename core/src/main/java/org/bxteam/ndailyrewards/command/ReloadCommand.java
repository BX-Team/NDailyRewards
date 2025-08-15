package org.bxteam.ndailyrewards.command;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.configuration.Language;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"reward", "rw", "ndailyrewards", "ndr"})
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ReloadCommand {
    private final NDailyRewards plugin;

    @Subcommand("reload")
    @CommandPermission("ndailyrewards.reload")
    void reload(CommandSender sender) {
        try {
            this.plugin.reload();
            sender.sendMessage(Language.PREFIX.asColoredString() + Language.COMMANDS_RELOAD.asColoredString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
