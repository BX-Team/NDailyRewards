package org.bxteam.ndailyrewards.manager;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.configuration.PluginConfig;
import org.bxteam.ndailyrewards.command.*;
import org.bxteam.ndailyrewards.command.lamp.LampExceptionHandler;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

@Singleton
public class CommandManager {
    private final Injector injector;
    private final Lamp<BukkitCommandActor> lamp;

    public CommandManager(NDailyRewards plugin, Injector injector) {
        this.injector = injector;
        PluginConfig config = injector.getInstance(PluginConfig.class);

        this.lamp = BukkitLamp.builder(plugin)
                .exceptionHandler(injector.getInstance(LampExceptionHandler.class))
                .suggestionProviders(providers -> {
                    providers.addProvider(Integer.class, context -> config.rewards.days.keySet().stream()
                            .map(String::valueOf)
                            .toList());
                })
                .build();
    }

    public void registerCommands() {
        this.lamp.register(
                this.injector.getInstance(ClaimCommand.class),
                this.injector.getInstance(HelpCommand.class),
                this.injector.getInstance(MainCommand.class),
                this.injector.getInstance(ReloadCommand.class),
                this.injector.getInstance(SetDayCommand.class),
                this.injector.getInstance(VersionCommand.class)
        );
    }

    public void unregisterCommands() {
        if (this.lamp != null) {
            this.lamp.unregisterAllCommands();
        }
    }
}

