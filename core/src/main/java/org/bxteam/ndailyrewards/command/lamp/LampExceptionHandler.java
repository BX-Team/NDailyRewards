package org.bxteam.ndailyrewards.command.lamp;

import org.bxteam.ndailyrewards.configuration.Language;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.exception.BukkitExceptionHandler;
import revxrsal.commands.bukkit.exception.InvalidPlayerException;
import revxrsal.commands.bukkit.exception.SenderNotPlayerException;
import revxrsal.commands.exception.MissingArgumentException;
import revxrsal.commands.exception.NoPermissionException;
import revxrsal.commands.node.ParameterNode;

public class LampExceptionHandler extends BukkitExceptionHandler {
    @Override
    public void onInvalidPlayer(InvalidPlayerException e, BukkitCommandActor actor) {
        actor.error(Language.PREFIX.asColoredString() + Language.PLAYER_NOT_FOUND.asColoredString());
    }

    @Override
    public void onSenderNotPlayer(SenderNotPlayerException e, BukkitCommandActor actor) {
        actor.error(Language.PREFIX.asColoredString() + Language.NOT_PLAYER.asColoredString());
    }

    @Override
    public void onNoPermission(@NotNull NoPermissionException e, @NotNull BukkitCommandActor actor) {
        actor.error(Language.PREFIX.asColoredString() + Language.NO_PERMISSION.asColoredString());
    }

    @Override
    public void onMissingArgument(@NotNull MissingArgumentException e, @NotNull BukkitCommandActor actor, @NotNull ParameterNode<BukkitCommandActor, ?> parameter) {
        actor.error(Language.PREFIX.asColoredString() + Language.INVALID_SYNTAX.asColoredString());
    }
}
