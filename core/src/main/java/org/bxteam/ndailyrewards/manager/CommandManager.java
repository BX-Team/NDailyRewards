package org.bxteam.ndailyrewards.manager;

import com.google.inject.Injector;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.argument.ArgumentKey;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import dev.rollczi.litecommands.bukkit.LiteBukkitMessages;
import dev.rollczi.litecommands.folia.FoliaExtension;
import dev.rollczi.litecommands.message.LiteMessages;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.commands.Commands;
import org.bxteam.ndailyrewards.commands.argument.SetDayArgument;
import org.bxteam.ndailyrewards.configuration.Language;

@RequiredArgsConstructor
public class CommandManager {
    private final Injector injector;
    private LiteCommands<CommandSender> liteCommands;

    public void registerCommands(NDailyRewards plugin) {
        this.liteCommands = LiteBukkitFactory.builder("ndailyrewards", plugin)
                .commands(this.injector.getInstance(Commands.class))
                .argument(Integer.class, ArgumentKey.of(SetDayArgument.KEY), this.injector.getInstance(SetDayArgument.class))

                .message(LiteMessages.MISSING_PERMISSIONS, Language.PREFIX.asColoredString() + Language.NO_PERMISSION.asColoredString())
                .message(LiteMessages.INVALID_USAGE, Language.PREFIX.asColoredString() + Language.INVALID_SYNTAX.asColoredString())
                .message(LiteBukkitMessages.PLAYER_NOT_FOUND, Language.PREFIX.asColoredString() + Language.PLAYER_NOT_FOUND.asColoredString())
                .message(LiteBukkitMessages.PLAYER_ONLY, Language.PREFIX.asColoredString() + Language.NOT_PLAYER.asColoredString())

                .extension(new FoliaExtension(plugin))

                .build();
    }

    public void unRegisterCommands() {
        if (this.liteCommands != null) {
            this.liteCommands.unregister();
        }
    }
}

