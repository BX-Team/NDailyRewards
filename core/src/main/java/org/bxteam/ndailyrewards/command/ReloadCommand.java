package org.bxteam.ndailyrewards.command;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bxteam.helix.logger.ExtendedLogger;
import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.configuration.Language;
import org.bxteam.ndailyrewards.messaging.MessageService;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"reward", "rw", "ndailyrewards", "ndr"})
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ReloadCommand {
    private final NDailyRewards plugin;
    private final ExtendedLogger logger;
    private final MessageService messageService;

    @Subcommand("reload")
    @CommandPermission("ndailyrewards.reload")
    void reload(CommandSender sender) {
        try {
            this.plugin.reload();
            messageService.send(sender, Language.COMMANDS_RELOAD);
        } catch (Exception e) {
            logger.error("Failed to reload plugin: %s".formatted(e.getMessage()));
            e.printStackTrace();
            messageService.sendPrefixed(sender, "&cReload failed, check console for details.");
        }
    }
}
