package space.bxteam.ndailyrewards.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.managers.command.SubCommand;
import space.bxteam.ndailyrewards.managers.enums.Language;
import space.bxteam.ndailyrewards.utils.Permissions;

import java.util.ArrayList;
import java.util.List;

public class SetDayCommand implements SubCommand {
    @Override
    public String getName() {
        return "setday";
    }

    @Override
    public String getDescription() {
        return "Set the day of a player";
    }

    @Override
    public String getSyntax() {
        return "/reward setday <player> <day>";
    }

    @Override
    public String getPermission() {
        return Permissions.SETDAY;
    }

    @Override
    public List<String> getTabCompletion(CommandSender sender, int index, String[] args) {
        if (index == 0) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames;
        }

        if (index == 1) {
            ConfigurationSection rewards = NDailyRewards.getInstance().getConfig().getConfigurationSection("rewards.days");
            List<String> days = new ArrayList<>();
            if (rewards != null) {
                for (String day : rewards.getKeys(false)) {
                    days.add(day);
                }
            }
            return days;
        }

        return null;
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(Language.PREFIX.asColoredString() + Language.INVALID_SYNTAX.asColoredString());
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(Language.PREFIX.asColoredString() + Language.PLAYER_NOT_FOUND.asColoredString());
            return;
        }

        try {
            int day = Integer.parseInt(args[1]);
            NDailyRewards.getInstance().getRewardManager().setDay(player, day - 1);
            sender.sendMessage(Language.PREFIX.asColoredString() + Language.COMMANDS_SETDAY.asColoredString()
                    .replace("<player>", player.getName())
                    .replace("<day>", String.valueOf(day)));
        } catch (NumberFormatException e) {
            sender.sendMessage(Language.PREFIX.asColoredString() + Language.INVALID_SYNTAX.asColoredString());
            return;
        }
    }
}
