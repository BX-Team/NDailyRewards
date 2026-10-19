package org.bxteam.ndailyrewards.command;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.configuration.Messages;
import org.bxteam.ndailyrewards.messaging.MessageService;
import org.bxteam.quark.common.QuarkLogger;
import org.bxteam.quark.config.ConfigException;
import org.bxteam.quark.config.validation.ConfigValidationException;
import org.bxteam.quark.config.validation.Violation;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.stream.Collectors;

@Command({"reward", "rw", "ndailyrewards", "ndr"})
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ReloadCommand {
    private final NDailyRewards plugin;
    private final QuarkLogger logger;
    private final MessageService messageService;
    private final Messages messages;

    @Subcommand("reload")
    @CommandPermission("ndailyrewards.reload")
    void reload(CommandSender sender) {
        try {
            this.plugin.reload();
            messageService.send(sender, messages.commands.reload);
        } catch (ConfigValidationException e) {
            logger.warn("{}", e.getMessage());
            String violations = e.violations().stream().map(Violation::toString).collect(Collectors.joining("; "));
            messageService.send(sender, messages.commands.reloadFailed, "<error>", violations);
        } catch (ConfigException e) {
            logger.warn("{}", e.getMessage());
            messageService.send(sender, messages.commands.reloadFailed, "<error>", e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to reload the plugin", e);
            messageService.send(sender, messages.commands.reloadFailed, "<error>", String.valueOf(e.getMessage()));
        }
    }
}
