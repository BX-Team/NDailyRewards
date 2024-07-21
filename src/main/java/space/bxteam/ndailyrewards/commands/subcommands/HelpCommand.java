package space.bxteam.ndailyrewards.commands.subcommands;

import org.bukkit.command.CommandSender;
import space.bxteam.ndailyrewards.managers.command.SubCommand;
import space.bxteam.ndailyrewards.managers.enums.Language;
import space.bxteam.ndailyrewards.utils.Permissions;

import java.util.List;

public class HelpCommand implements SubCommand {
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Shows the help message";
    }

    @Override
    public String getSyntax() {
        return "/reward help";
    }

    @Override
    public String getPermission() {
        return Permissions.HELP;
    }

    @Override
    public List<String> getTabCompletion(CommandSender sender, int index, String[] args) {
        return null;
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        for (String message : Language.HELP.asColoredStringList()) {
            sender.sendMessage(message);
        }
    }
}
