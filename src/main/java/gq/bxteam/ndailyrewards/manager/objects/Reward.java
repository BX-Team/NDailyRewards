package gq.bxteam.ndailyrewards.manager.objects;

import org.bukkit.ChatColor;
import gq.bxteam.ndailyrewards.utils.ArchUtils;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reward
{
    private final int day;
    private final List<String> lore;
    private final List<String> cmds;
    private final List<String> msg;

    public Reward(final int day, final List<String> lore, final List<String> cmds, final List<String> msg) {
        this.day = day;
        this.lore = lore;
        this.cmds = cmds;
        this.msg = msg;
    }
    
    public int getDay() {
        return this.day;
    }
    
    public List<String> getLore() {
        return this.lore;
    }
    
    public List<String> getCommands() {
        return this.cmds;
    }
    
    public List<String> getMessages() {
        return this.msg;
    }
    
    public void give(final Player p) {
        for (final String s : this.cmds) {
            ArchUtils.execCmd(s.replace("%day%", String.valueOf(this.day)), p);
        }
        for (String s : this.msg) {
            Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
            Matcher matcher = pattern.matcher(s);
            while (matcher.find()) {
                String hexCode = s.substring(matcher.start(), matcher.end());
                String replaceSharp = hexCode.replace('#', 'x');
                char[] ch = replaceSharp.toCharArray();
                StringBuilder builder = new StringBuilder("");
                for (char c : ch)
                    builder.append("&" + c);
                s = s.replace(hexCode, builder.toString());
                matcher = pattern.matcher(s);
            }
            String pref = ChatColor.translateAlternateColorCodes('&', s);
            p.sendMessage(pref.replace("%day%", String.valueOf(this.day)));
        }
    }
}
