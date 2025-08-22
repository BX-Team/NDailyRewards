package org.bxteam.ndailyrewards.command;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bxteam.helix.updater.VersionFetcher;
import org.bxteam.ndailyrewards.configuration.Language;
import org.bxteam.ndailyrewards.utils.TextUtils;
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

    @Subcommand("version")
    @CommandPermission("ndailyrewards.version")
    void version(CommandSender sender) {
        final var current = new ComparableVersion(this.pluginDescription.getVersion());

        sender.sendMessage(Language.PREFIX.asColoredString() + TextUtils.applyColor("&aCurrent installed version: &e" + current));

        supplyAsync(this.versionFetcher::fetchNewestVersion).thenApply(Objects::requireNonNull).whenComplete((newest, error) -> {
            if (error != null || newest.compareTo(current) <= 0) {
                return;
            }

            sender.sendMessage(Language.PREFIX.asColoredString() + TextUtils.applyColor("&aA new update is available: &e" + newest));
            sender.sendMessage(Language.PREFIX.asColoredString() + TextUtils.applyColor("&aDownload here: &e" + this.versionFetcher.getDownloadUrl()));
        });
    }
}
