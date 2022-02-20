package me.nonplay.ndailyrewards.manager.objects;

import org.bukkit.ChatColor;
import me.nonplay.ndailyrewards.utils.ArchUtils;
import org.bukkit.entity.Player;
import java.util.List;

public class Reward
{
    private int day;
    private List<String> lore;
    private List<String> cmds;
    private List<String> msg;
    
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
        for (final String s : this.msg) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', s.replace("%day%", String.valueOf(this.day))));
        }
    }
}
