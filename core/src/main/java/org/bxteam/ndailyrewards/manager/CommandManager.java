package org.bxteam.ndailyrewards.manager;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.bukkit.configuration.ConfigurationSection;
import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.command.*;
import org.bxteam.ndailyrewards.command.lamp.LampExceptionHandler;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.util.List;

@Singleton
public class CommandManager {
    private final Injector injector;
    private final Lamp<BukkitCommandActor> lamp;

    public CommandManager(NDailyRewards plugin, Injector injector) {
        this.injector = injector;

        this.lamp = BukkitLamp.builder(plugin)
                .exceptionHandler(new LampExceptionHandler())
                .suggestionProviders(providers -> {
                    providers.addProvider(Integer.class, context -> {
                        ConfigurationSection rewards = plugin.getConfig().getConfigurationSection("rewards.days");
                        if (rewards == null) return List.of();
                        return rewards.getKeys(false).stream()
                                .map(Integer::parseInt)
                                .map(String::valueOf)
                                .toList();
                    });
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

