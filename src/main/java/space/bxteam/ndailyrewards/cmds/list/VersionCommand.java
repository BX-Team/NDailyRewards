package space.bxteam.ndailyrewards.cmds.list;

import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.cfg.Lang;
import space.bxteam.ndailyrewards.cmds.ICmd;
import space.bxteam.ndailyrewards.utils.TextUtils;
import org.bukkit.command.CommandSender;

public class VersionCommand extends ICmd
{
    public VersionCommand(final NDailyRewards plugin) {
        super(plugin);
    }

    @Override
    public void perform(final CommandSender sender, final String[] args) {
        sender.sendMessage(Lang.Prefix.toMsg() + "Current installed version is: " + NDailyRewards.getInstance().getDescription().getVersion());
        NDailyRewards.checkForUpdates().ifPresent(latestVersion -> {
            sender.sendMessage(TextUtils.applyColor("&aAn update is available: " + latestVersion));
            sender.sendMessage(TextUtils.applyColor("&aDownload here: https://modrinth.com/plugin/ndailyrewards/version/" + latestVersion));
        });
    }

    @Override
    public String getPermission() {
        return "ndailyrewards.user";
    }

    @Override
    public boolean playersOnly() {
        return false;
    }

    @Override
    public String label() {
        return "version";
    }

    @Override
    public String usage() {
        return "";
    }
}
