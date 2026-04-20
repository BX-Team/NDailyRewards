package org.bxteam.ndailyrewards.command;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bxteam.helix.updater.VersionFetcher;
import org.bxteam.ndailyrewards.messaging.MessageService;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.Objects;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Command({"reward", "rw", "ndailyrewards", "ndr"})
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VersionCommand {
    private final PluginDescriptionFile pluginDescription;
    private final VersionFetcher versionFetcher;
    private final MessageService messageService;

    @Subcommand("version")
    @CommandPermission("ndailyrewards.version")
    void version(CommandSender sender) {
        final var current = new ComparableVersion(this.pluginDescription.getVersion());

        messageService.sendPrefixed(sender, "&aCurrent installed version: &e" + current);

        supplyAsync(this.versionFetcher::fetchNewestVersion).thenApply(Objects::requireNonNull).whenComplete((newest, error) -> {
            if (error != null || newest.compareTo(current) <= 0) {
                return;
            }

            messageService.sendPrefixed(sender, "&aA new update is available: &e" + newest);
            messageService.sendPrefixed(sender, "&aDownload here: &e" + this.versionFetcher.getDownloadUrl());
        });
    }
}
