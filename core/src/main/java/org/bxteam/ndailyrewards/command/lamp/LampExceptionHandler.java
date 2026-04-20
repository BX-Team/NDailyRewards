package org.bxteam.ndailyrewards.command.lamp;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bxteam.ndailyrewards.configuration.Language;
import org.bxteam.ndailyrewards.messaging.MessageService;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.exception.BukkitExceptionHandler;
import revxrsal.commands.bukkit.exception.InvalidPlayerException;
import revxrsal.commands.bukkit.exception.SenderNotPlayerException;
import revxrsal.commands.exception.MissingArgumentException;
import revxrsal.commands.exception.NoPermissionException;
import revxrsal.commands.node.ParameterNode;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LampExceptionHandler extends BukkitExceptionHandler {
    private final MessageService messageService;

    @Override
    public void onInvalidPlayer(InvalidPlayerException e, BukkitCommandActor actor) {
        messageService.send(actor.sender(), Language.PLAYER_NOT_FOUND);
    }

    @Override
    public void onSenderNotPlayer(SenderNotPlayerException e, BukkitCommandActor actor) {
        messageService.send(actor.sender(), Language.NOT_PLAYER);
    }

    @Override
    public void onNoPermission(@NotNull NoPermissionException e, @NotNull BukkitCommandActor actor) {
        messageService.send(actor.sender(), Language.NO_PERMISSION);
    }

    @Override
    public void onMissingArgument(@NotNull MissingArgumentException e, @NotNull BukkitCommandActor actor, @NotNull ParameterNode<BukkitCommandActor, ?> parameter) {
        messageService.send(actor.sender(), Language.INVALID_SYNTAX);
    }
}
