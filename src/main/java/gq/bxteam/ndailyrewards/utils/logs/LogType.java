package gq.bxteam.ndailyrewards.utils.logs;

import org.bukkit.ChatColor;

public enum LogType
{
    INFO("INFO", 0, ChatColor.WHITE), 
    WARN("WARN", 1, ChatColor.GOLD),
    ERROR("ERROR", 2, ChatColor.RED), 
    DEBUG("DEBUG", 3, ChatColor.AQUA);
    
    private final ChatColor c;
    
    LogType(final String name, final int ordinal, final ChatColor c) {
        this.c = c;
    }
    
    public ChatColor color() {
        return this.c;
    }
}
