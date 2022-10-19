package gq.bxteam.ndailyrewards.cmds.list;

import gq.bxteam.ndailyrewards.NDailyRewards;
import gq.bxteam.ndailyrewards.cfg.Lang;
import gq.bxteam.ndailyrewards.cmds.ICmd;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends ICmd
{
    public ReloadCommand(final NDailyRewards plugin) {
        super(plugin);
    }
    
    @Override
    public void perform(final CommandSender sender, final String[] args) {
        this.plugin.reload();
        sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Command_Reload_Exec.toMsg());
    }
    
    @Override
    public String getPermission() {
        return "ndailyrewards.admin";
    }
    
    @Override
    public boolean playersOnly() {
        return false;
    }
    
    @Override
    public String label() {
        return "reload";
    }
    
    @Override
    public String usage() {
        return "";
    }
}
