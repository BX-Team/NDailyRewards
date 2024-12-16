package space.bxteam.ndailyrewards.commands.subcommands;

import org.bukkit.command.CommandSender;
import space.bxteam.commons.github.*;
import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.managers.command.SubCommand;
import space.bxteam.ndailyrewards.managers.enums.Language;
import space.bxteam.ndailyrewards.utils.Permissions;
import space.bxteam.ndailyrewards.utils.TextUtils;

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
        sender.sendMessage(Language.PREFIX.asColoredString() + TextUtils.applyColor("&aCurrent installed version: &e" + NDailyRewards.getInstance().getDescription().getVersion()));
        GitCheck gitCheck = new GitCheck();
        GitRepository repository = GitRepository.of("BX-Team", "NDailyRewards");

        GitCheckResult result = gitCheck.checkRelease(repository, GitTag.of("v" + NDailyRewards.getInstance().getDescription().getVersion()));
        if (!result.isUpToDate()) {
            GitRelease release = result.getLatestRelease();
            GitTag tag = release.getTag();

            sender.sendMessage(Language.PREFIX.asColoredString() + TextUtils.applyColor("&aA new update is available: &e" + tag.getTag()));
            sender.sendMessage(Language.PREFIX.asColoredString() + TextUtils.applyColor("&aDownload here: &e" + release.getPageUrl()));
        }
    }
}
