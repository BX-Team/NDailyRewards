package me.nonplay.ndailyrewards.cmds.list;

import java.util.Iterator;
import net.md_5.bungee.api.ChatColor;
import me.nonplay.ndailyrewards.cfg.Lang;
import org.bukkit.command.CommandSender;
import me.nonplay.ndailyrewards.NDailyRewards;
import me.nonplay.ndailyrewards.cmds.ICmd;

public class HelpCommand extends ICmd
{
    public HelpCommand(final NDailyRewards plugin) {
        super(plugin);
    }
    
    @Override
    public void perform(final CommandSender sender, final String[] args) {
        if (args.length <= 1) {
            for (final String s : Lang.Commands_Help_Commands.getList()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
            }
        }
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
        return "help";
    }
    
    @Override
    public String usage() {
        return "";
    }
}
