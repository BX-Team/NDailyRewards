package org.bxteam.ndailyrewards.command;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bxteam.ndailyrewards.configuration.PluginConfig;
import org.bxteam.ndailyrewards.manager.menu.MenuManager;
import revxrsal.commands.annotation.Command;

@Command({"reward", "rw", "ndailyrewards", "ndr"})
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MainCommand {
    private final PluginConfig config;
    private final MenuManager menuManager;

    @Command({"reward", "rw", "ndailyrewards", "ndr"})
    void execute(Player sender) {
        this.menuManager.openRewardsMenu(sender);

        if (this.config.sound.open.enabled) {
            this.config.sound.open.type.play(sender);
        }
    }
}
