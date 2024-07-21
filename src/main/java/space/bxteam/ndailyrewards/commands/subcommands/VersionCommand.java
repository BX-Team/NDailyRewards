package space.bxteam.ndailyrewards.commands.subcommands;

import org.bukkit.command.CommandSender;
import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.managers.command.SubCommand;
import space.bxteam.ndailyrewards.managers.enums.Language;
import space.bxteam.ndailyrewards.utils.Permissions;
import space.bxteam.ndailyrewards.utils.TextUtils;
import space.bxteam.ndailyrewards.utils.UpdateCheckerUtil;

import java.util.List;

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
        sender.sendMessage(Language.PREFIX.asColoredString() + TextUtils.applyColor("&aCurrent installed version: " + NDailyRewards.getInstance().getPluginMeta().getVersion()));
        UpdateCheckerUtil.checkForUpdates().ifPresent(latestVersion -> {
            sender.sendMessage(Language.PREFIX.asColoredString() + TextUtils.applyColor("&aA new update is available: " + latestVersion));
            sender.sendMessage(Language.PREFIX.asColoredString() + TextUtils.applyColor("&aDownload here: &ehttps://modrinth.com/plugin/ndailyrewards/version/" + latestVersion));
        });
    }
}
