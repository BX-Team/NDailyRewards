package space.bxteam.ndailyrewards.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.managers.command.SubCommand;
import space.bxteam.ndailyrewards.managers.enums.Language;
import space.bxteam.ndailyrewards.managers.reward.RewardManager;
import space.bxteam.ndailyrewards.utils.Permissions;

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
        if (!(sender instanceof Player)) {
            sender.sendMessage(Language.PREFIX.asColoredString() + Language.NOT_PLAYER.asColoredString());
            return;
        }

        Player player = (Player) sender;
        RewardManager rewardManager = NDailyRewards.getInstance().getRewardManager();
        RewardManager.PlayerRewardData playerRewardData = rewardManager.getPlayerRewardData(player.getUniqueId());

        int currentDay = playerRewardData.getCurrentDay();
        int nextDay = currentDay + 1;

        if (System.currentTimeMillis() >= playerRewardData.getNext()) {
            RewardManager.Reward reward = rewardManager.getReward(nextDay);
            if (reward != null) {
                rewardManager.giveReward(player, nextDay);
            } else if (rewardManager.shouldResetWhenAllClaimed() && rewardManager.getReward(1) != null) {
                rewardManager.giveReward(player, 1);
            } else {
                player.sendMessage(Language.PREFIX.asColoredString() + Language.CLAIM_NOT_AVAILABLE.asColoredString());
            }
        } else {
            player.sendMessage(Language.PREFIX.asColoredString() + Language.CLAIM_NOT_AVAILABLE.asColoredString());
        }
    }
}
