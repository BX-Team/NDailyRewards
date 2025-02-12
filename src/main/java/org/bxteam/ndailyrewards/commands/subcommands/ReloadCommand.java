package org.bxteam.ndailyrewards.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.managers.command.SubCommand;
import org.bxteam.ndailyrewards.managers.enums.Language;
import org.bxteam.ndailyrewards.utils.Permissions;

import java.util.List;

public class ReloadCommand implements SubCommand {
    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads the plugin";
    }

    @Override
    public String getSyntax() {
        return "/reward reload";
    }

    @Override
    public String getPermission() {
        return Permissions.RELOAD;
    }

    @Override
    public List<String> getTabCompletion(CommandSender sender, int index, String[] args) {
        return null;
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        try {
            NDailyRewards.getInstance().reload();
            sender.sendMessage(Language.PREFIX.asColoredString() + Language.COMMANDS_RELOAD.asColoredString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
