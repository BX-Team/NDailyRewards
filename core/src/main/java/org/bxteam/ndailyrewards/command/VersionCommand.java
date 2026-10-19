package org.bxteam.ndailyrewards.command;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bxteam.ndailyrewards.configuration.Messages;
import org.bxteam.ndailyrewards.messaging.MessageService;
import org.bxteam.quark.update.UpdateChecker;
import org.bxteam.quark.update.UpdateStatus;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"reward", "rw", "ndailyrewards", "ndr"})
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VersionCommand {
    private final UpdateChecker updateChecker;
    private final MessageService messageService;
    private final Messages messages;

    @Subcommand("version")
    @CommandPermission("ndailyrewards.version")
    void version(CommandSender sender) {
        messageService.send(sender, messages.commands.version, "<version>", this.updateChecker.currentVersion().toString());

        this.updateChecker.check().thenAccept(status -> {
            if (status instanceof UpdateStatus.Outdated outdated) {
                messageService.sendRaw(sender, messageService.updateNotice(outdated.latest()));
            }
        });
    }
}
