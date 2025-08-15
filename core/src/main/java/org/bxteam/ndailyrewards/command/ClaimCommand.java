package org.bxteam.ndailyrewards.command;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bxteam.ndailyrewards.configuration.Language;
import org.bxteam.ndailyrewards.manager.reward.RewardManager;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"reward", "rw", "ndailyrewards", "ndr"})
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ClaimCommand {
    private final RewardManager rewardManager;

    @Subcommand("claim")
    @CommandPermission("ndailyrewards.claim")
    void claim(Player sender) {
        this.rewardManager.getPlayerRewardDataAsync(sender.getUniqueId())
            .thenAccept(rewardData -> {
                if (rewardData == null) {
                    sender.sendMessage(Language.PREFIX.asColoredString() + Language.CLAIM_NOT_AVAILABLE.asColoredString());
                    return;
                }

                int nextDay = rewardData.currentDay() + 1;
                if (this.rewardManager.isRewardAvailable(rewardData, nextDay)) {
                    this.rewardManager.giveReward(sender, nextDay);
                } else {
                    sender.sendMessage(Language.PREFIX.asColoredString() + Language.CLAIM_NOT_AVAILABLE.asColoredString());
                }
            });
    }
}
