package org.bxteam.ndailyrewards.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.managers.command.SubCommand;
import org.bxteam.ndailyrewards.managers.enums.Language;
import org.bxteam.ndailyrewards.managers.reward.PlayerRewardData;
import org.bxteam.ndailyrewards.managers.reward.RewardManager;
import org.bxteam.ndailyrewards.utils.Permissions;

import java.util.List;

public class ClaimCommand implements SubCommand {
    @Override
    public String getName() {
        return "claim";
    }

    @Override
    public String getDescription() {
        return "Claims an available reward";
    }

    @Override
    public String getSyntax() {
        return "/reward claim";
    }

    @Override
    public String getPermission() {
        return Permissions.CLAIM;
    }

    @Override
    public List<String> getTabCompletion(CommandSender sender, int index, String[] args) {
        return null;
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Language.PREFIX.asColoredString() + Language.NOT_PLAYER.asColoredString());
            return;
        }

        RewardManager rewardManager = NDailyRewards.getInstance().getRewardManager();
        PlayerRewardData rewardData = rewardManager.getPlayerRewardData(player.getUniqueId());
        int nextDay = rewardData.currentDay() + 1;

        if (rewardManager.isRewardAvailable(rewardData, nextDay)) {
            rewardManager.giveReward(player, nextDay);
        } else {
            player.sendMessage(Language.PREFIX.asColoredString() + Language.CLAIM_NOT_AVAILABLE.asColoredString());
        }
    }
}
