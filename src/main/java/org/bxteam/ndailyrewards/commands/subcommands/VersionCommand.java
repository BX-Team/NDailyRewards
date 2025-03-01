package org.bxteam.ndailyrewards.commands.subcommands;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.command.CommandSender;
import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.managers.command.SubCommand;
import org.bxteam.ndailyrewards.managers.enums.Language;
import org.bxteam.ndailyrewards.utils.Permissions;
import org.bxteam.ndailyrewards.utils.TextUtils;

import java.util.List;
import java.util.Objects;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class VersionCommand implements SubCommand {
    @Override
    public String getName() {
        return "version";
    }

    @Override
    public String getDescription() {
        return "Shows the plugin version and sends message if there is an update available";
    }

    @Override
    public String getSyntax() {
        return "/reward version";
    }

    @Override
    public String getPermission() {
        return Permissions.VERSION;
    }

    @Override
    public List<String> getTabCompletion(CommandSender sender, int index, String[] args) {
        return null;
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        final var current = new ComparableVersion(NDailyRewards.getInstance().getDescription().getVersion());

        sender.sendMessage(Language.PREFIX.asColoredString() + TextUtils.applyColor("&aCurrent installed version: &e" + current));

        supplyAsync(NDailyRewards.getInstance().getVersionFetcher()::fetchNewestVersion).thenApply(Objects::requireNonNull).whenComplete((newest, error) -> {
            if (error != null || newest.compareTo(current) <= 0) {
                return;
            }

            sender.sendMessage(Language.PREFIX.asColoredString() + TextUtils.applyColor("&aA new update is available: &e" + newest));
            sender.sendMessage(Language.PREFIX.asColoredString() + TextUtils.applyColor("&aDownload here: &e" + NDailyRewards.getInstance().getVersionFetcher().getDownloadUrl()));
        });
    }
}
