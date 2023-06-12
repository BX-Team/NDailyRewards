package gq.bxteam.ndailyrewards.cmds;

import gq.bxteam.ndailyrewards.utils.ArchUtils;
import gq.bxteam.ndailyrewards.cfg.Lang;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;
import org.bukkit.entity.Player;
import gq.bxteam.ndailyrewards.NDailyRewards;

public abstract class ICmd
{
    protected NDailyRewards plugin;
    
    public ICmd(final NDailyRewards plugin) {
        this.plugin = plugin;
    }
    
    public abstract String label();
    
    public abstract String getPermission();
    
    public abstract boolean playersOnly();
    
    public abstract String usage();
    
    public List<String> getTab(final Player p, final int i, final String[] args) {
        return Collections.emptyList();
    }
    
    public abstract void perform(final CommandSender p0, final String[] p1);
    
    public final void execute(final CommandSender sender, final String[] args) {
        if (this.playersOnly() && !(sender instanceof Player)) {
            return;
        }
        if (!this.hasPerm(sender)) {
            sender.sendMessage(Lang.Prefix.toMsg() + Lang.Error_NoPerm.toMsg());
            return;
        }
        this.perform(sender, args);
    }
    
    public boolean hasPerm(final CommandSender sender) {
        if (this.getPermission() == null) {
            return true;
        }
        if (sender instanceof Player p) {
            return p.hasPermission(this.getPermission());
        }
        return true;
    }
    
    protected void printUsage(final CommandSender sender) {
        sender.sendMessage(ArchUtils.oneSpace(Lang.Prefix.toMsg() + Lang.Commands_Help_Usage.toMsg().replace("%usage%", this.usage()).replace("%command%", this.label())));
    }
    
    protected void errPerm(final CommandSender sender) {
        sender.sendMessage(Lang.Prefix + Lang.Error_NoPerm.toMsg());
    }
    
    protected void errPlayer(final CommandSender sender) {
        sender.sendMessage(Lang.Prefix + Lang.Error_NoPlayer.toMsg());
    }
    
    protected void errSender(final CommandSender sender) {
        sender.sendMessage(Lang.Prefix + Lang.Error_Console.toMsg());
    }
    
    protected double getNumD(final CommandSender sender, final String input, final double def) {
        return this.getNumD(sender, input, def, false);
    }
    
    protected double getNumD(final CommandSender sender, final String input, final double def, final boolean allowNega) {
        try {
            final double amount = Double.parseDouble(input);
            if (amount < 0.0 && !allowNega) {
                throw new NumberFormatException();
            }
            return amount;
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(Lang.Prefix + Lang.Error_Number.toMsg().replace("%num%", input));
            return def;
        }
    }
    
    protected int getNumI(final CommandSender sender, final String input, final int def) {
        return this.getNumI(sender, input, def, false);
    }
    
    protected int getNumI(final CommandSender sender, final String input, final int def, final boolean nega) {
        return (int)this.getNumD(sender, input, def, nega);
    }
}
