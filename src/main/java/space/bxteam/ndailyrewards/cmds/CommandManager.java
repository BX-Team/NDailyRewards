package space.bxteam.ndailyrewards.cmds;

import java.util.Collections;

import space.bxteam.ndailyrewards.cmds.list.*;
import space.bxteam.ndailyrewards.utils.ArchUtils;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import space.bxteam.ndailyrewards.NDailyRewards;
import org.bukkit.command.TabExecutor;
import org.bukkit.command.CommandExecutor;

public class CommandManager implements CommandExecutor, TabExecutor
{
    private final NDailyRewards plugin;
    private Map<String, ICmd> commands;
    private OpenCommand open;
    
    public CommandManager(final NDailyRewards plugin) {
        this.plugin = plugin;
    }
    
    public void setup() {
        this.commands = new LinkedHashMap<String, ICmd>();
        this.register(this.open = new OpenCommand(this.plugin));
        this.register(new HelpCommand(this.plugin));
        this.register(new ReloadCommand(this.plugin));
        this.register(new VersionCommand(this.plugin));
        this.register(new BackupCommand(this.plugin));
    }
    
    public void shutdown() {
        this.commands.clear();
    }
    
    public void register(final ICmd cmd) {
        this.commands.put(cmd.label(), cmd);
    }
    
    public Collection<ICmd> getCommands() {
        return this.commands.values();
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        ICmd command = this.open;
        if (args.length > 0 && this.commands.containsKey(args[0])) {
            command = this.commands.get(args[0]);
        }
        command.execute(sender, args);
        return true;
    }
    
    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }
        if (args.length == 0) {
            return null;
        }
        if (args.length == 1) {
            final List<String> sugg = new ArrayList<String>(this.commands.keySet());
            for (final ICmd j : this.commands.values()) {
                if (!j.hasPerm(sender)) {
                    sugg.remove(j.label());
                }
            }
            return ArchUtils.getSugg(args[0], sugg);
        }
        final ICmd cb = this.commands.get(args[0]);
        if (cb == null) {
            return Collections.emptyList();
        }
        final List<String> list = cb.getTab((Player)sender, args.length - 1, args);
        return ArchUtils.getSugg(args[args.length - 1], list);
    }
}
