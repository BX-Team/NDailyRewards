package gq.bxteam.ndailyrewards.cmds.list;

import gq.bxteam.ndailyrewards.NDailyRewards;
import gq.bxteam.ndailyrewards.cfg.Config;
import gq.bxteam.ndailyrewards.cmds.ICmd;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

public class OpenCommand extends ICmd
{
    public OpenCommand(final NDailyRewards plugin) {
        super(plugin);
    }
    
    @Override
    public void perform(final CommandSender sender, final String[] args) {
        final Player p = (Player)sender;
        Config.rewards_gui.open(p);
    }
    
    @Override
    public String getPermission() {
        return "ndailyrewards.user";
    }
    
    @Override
    public boolean playersOnly() {
        return true;
    }
    
    @Override
    public String label() {
        return "open";
    }
    
    @Override
    public String usage() {
        return "";
    }
}
