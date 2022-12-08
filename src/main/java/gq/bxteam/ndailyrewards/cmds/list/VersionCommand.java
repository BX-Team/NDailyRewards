package gq.bxteam.ndailyrewards.cmds.list;

import gq.bxteam.ndailyrewards.NDailyRewards;
import gq.bxteam.ndailyrewards.cfg.Lang;
import gq.bxteam.ndailyrewards.cmds.ICmd;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

public class VersionCommand extends ICmd
{
    public VersionCommand(final NDailyRewards plugin) {
        super(plugin);
    }

    @Override
    public void perform(final CommandSender sender, final String[] args) {
        sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Command_Plugin_Version.toMsg());
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
