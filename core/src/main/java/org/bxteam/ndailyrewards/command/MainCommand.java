package org.bxteam.ndailyrewards.command;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.manager.menu.MenuManager;
import org.bxteam.ndailyrewards.utils.SoundUtil;
import revxrsal.commands.annotation.Command;

@Command({"reward", "rw", "ndailyrewards", "ndr"})
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MainCommand {
    private final NDailyRewards plugin;
    private final MenuManager menuManager;
    private final SoundUtil soundUtil;

    @Command({"reward", "rw", "ndailyrewards", "ndr"})
    void execute(Player sender) {
        this.menuManager.openRewardsMenu(sender);

        if (this.plugin.getConfig().getBoolean("sound.open.enabled")) {
            this.soundUtil.playSound(sender, "open");
        }
    }
}
