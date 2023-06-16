package gq.bxteam.ndailyrewards.cmds.list;

import gq.bxteam.ndailyrewards.NDailyRewards;
import gq.bxteam.ndailyrewards.cfg.Lang;
import gq.bxteam.ndailyrewards.cmds.ICmd;
import org.bukkit.command.CommandSender;

public class HelpCommand extends ICmd
{
    public HelpCommand(final NDailyRewards plugin) {
        super(plugin);
    }
    
    @Override
    public void perform(final CommandSender sender, final String[] args) {
        if (args.length <= 1) {
            for (String s : Lang.Commands_Help_Commands.getList()) {
                String performCmd = NDailyRewards.replaceHEXColorCode(s);
                sender.sendMessage(performCmd);
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
