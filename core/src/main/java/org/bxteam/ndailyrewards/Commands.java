package org.bxteam.ndailyrewards;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bxteam.ndailyrewards.managers.enums.Language;
import org.bxteam.ndailyrewards.managers.reward.PlayerRewardData;
import org.bxteam.ndailyrewards.managers.reward.RewardManager;
import org.bxteam.ndailyrewards.utils.SoundUtil;
import org.bxteam.ndailyrewards.utils.TextUtils;

import java.util.Objects;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Command(name = "reward", aliases = {"rw", "ndailyrewards", "ndr"})
public class Commands {
    @Execute
    void execute(@Context Player sender) {
        NDailyRewards.getInstance().getMenuManager().openRewardsMenu(sender);
        if (NDailyRewards.getInstance().getConfig().getBoolean("sound.open.enabled")) {
            SoundUtil.playSound(sender, "open");
        }
    }

    @Execute(name = "claim")
    @Permission("ndailyrewards.claim")
    void claim(@Context Player sender) {
        RewardManager rewardManager = NDailyRewards.getInstance().getRewardManager();
        PlayerRewardData rewardData = rewardManager.getPlayerRewardData(sender.getUniqueId());
        int nextDay = rewardData.currentDay() + 1;

        if (rewardManager.isRewardAvailable(rewardData, nextDay)) {
            rewardManager.giveReward(sender, nextDay);
        } else {
            sender.sendMessage(Language.PREFIX.asColoredString() + Language.CLAIM_NOT_AVAILABLE.asColoredString());
        }
    }


    @Execute(name = "help")
    @Permission("ndailyrewards.help")
    void help(@Context CommandSender sender) {
        for (String message : Language.COMMANDS_HELP.asColoredStringList()) {
            sender.sendMessage(message);
        }
    }

    @Execute(name = "reload")
    @Permission("ndailyrewards.reload")
    void reload(@Context CommandSender sender) {
        try {
            NDailyRewards.getInstance().reload();
            sender.sendMessage(Language.PREFIX.asColoredString() + Language.COMMANDS_RELOAD.asColoredString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Execute(name = "setday")
    @Permission("ndailyrewards.setday")
    void setDay(@Context CommandSender sender, @Arg Player target, @Arg int day) {
        try {
            NDailyRewards.getInstance().getRewardManager().setDay(target, day - 1);
            sender.sendMessage(Language.PREFIX.asColoredString() + Language.COMMANDS_SETDAY.asColoredString()
                    .replace("<player>", target.getName())
                    .replace("<day>", String.valueOf(day)));
        } catch (NumberFormatException e) {
            sender.sendMessage(Language.PREFIX.asColoredString() + Language.INVALID_SYNTAX.asColoredString());
        }
    }

    @Execute(name = "version")
    @Permission("ndailyrewards.version")
    void version(@Context CommandSender sender) {
        final var current = new ComparableVersion(NDailyRewards.getInstance().getDescription().getVersion());

        sender.sendMessage(Language.PREFIX.asColoredString() + TextUtils.applyColor("&aCurrent installed version: &e" + current));

        supplyAsync(NDailyRewards.getInstance().getVersionFetcher()::fetchNewestVersion).thenApply(Objects::requireNonNull).whenComplete((newest, error) -> {
            if (error != null || newest.compareTo(current) <= 0) {
                return;
            }

            sender.sendMessage(Language.PREFIX.asColoredString() + TextUtils.applyColor("&aA new update is available: &e" + newest));
            sender.sendMessage(Language.PREFIX.asColoredString() + TextUtils.applyColor("&aDownload here: &e" + NDailyRewards.getInstance().getVersionFetcher().getDownloadUrl()));
        });
    }
}
