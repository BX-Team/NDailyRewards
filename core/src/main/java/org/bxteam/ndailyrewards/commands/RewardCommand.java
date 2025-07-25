package org.bxteam.ndailyrewards.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.commands.subcommands.*;
import org.bxteam.ndailyrewards.managers.command.MainCommand;
import org.bxteam.ndailyrewards.managers.command.matcher.StringArgumentMatcher;
import org.bxteam.ndailyrewards.managers.enums.Language;
import org.bxteam.ndailyrewards.utils.SoundUtil;

public class RewardCommand extends MainCommand {
    public RewardCommand() {
        super(new StringArgumentMatcher());
    }

    @Override
    protected void registerSubCommands() {
        subCommands.add(new ClaimCommand());
        subCommands.add(new HelpCommand());
        subCommands.add(new ReloadCommand());
        subCommands.add(new SetDayCommand());
        subCommands.add(new VersionCommand());
    }

    @Override
    protected void perform(CommandSender sender) {
        if (sender instanceof Player player) {
            NDailyRewards.getInstance().getMenuManager().openRewardsMenu(player);
            if (NDailyRewards.getInstance().getConfig().getBoolean("sound.open.enabled")) {
                SoundUtil.playSound(player, "open");
            }
        } else {
            sender.sendMessage(Language.PREFIX.asColoredString() + Language.NOT_PLAYER.asColoredString());
        }
    }
}
