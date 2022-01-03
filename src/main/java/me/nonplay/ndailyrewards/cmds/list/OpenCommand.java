package me.nonplay.ndailyrewards.cmds.list;

import me.nonplay.ndailyrewards.cfg.Config;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import me.nonplay.ndailyrewards.NDailyRewards;
import me.nonplay.ndailyrewards.cmds.ICmd;

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
