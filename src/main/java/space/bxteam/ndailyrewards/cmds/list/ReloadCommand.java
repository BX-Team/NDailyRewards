package space.bxteam.ndailyrewards.cmds.list;

import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.cfg.Lang;
import space.bxteam.ndailyrewards.cmds.ICmd;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends ICmd
{
    public ReloadCommand(final NDailyRewards plugin) {
        super(plugin);
    }
    
    @Override
    public void perform(final CommandSender sender, final String[] args) {
        this.plugin.reload();
        sender.sendMessage(Lang.Prefix.toMsg() + Lang.Command_Reload_Exec.toMsg());
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
