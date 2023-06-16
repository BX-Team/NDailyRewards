package gq.bxteam.ndailyrewards.cmds.list;

import gq.bxteam.ndailyrewards.NDailyRewards;
import gq.bxteam.ndailyrewards.cfg.Lang;
import gq.bxteam.ndailyrewards.cmds.ICmd;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelpCommand extends ICmd
{
    public HelpCommand(final NDailyRewards plugin) {
        super(plugin);
    }
    
    @Override
    public void perform(final CommandSender sender, final String[] args) {
        if (args.length <= 1) {
            for (String s : Lang.Commands_Help_Commands.getList()) {
                Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
                Matcher matcher = pattern.matcher(s);
                while (matcher.find()) {
                    String hexCode = s.substring(matcher.start(), matcher.end());
                    String replaceSharp = hexCode.replace('#', 'x');
                    char[] ch = replaceSharp.toCharArray();
                    StringBuilder builder = new StringBuilder();
                    for (char c : ch)
                        builder.append("&" + c);
                    s = s.replace(hexCode, builder.toString());
                    matcher = pattern.matcher(s);
                }
                String performCmd = org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
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
