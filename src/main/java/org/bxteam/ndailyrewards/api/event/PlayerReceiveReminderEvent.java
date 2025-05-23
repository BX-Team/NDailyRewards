package org.bxteam.ndailyrewards.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called when the player received a reminder for the daily reward.
 *
 * @since 3.0.0
 */
public class PlayerReceiveReminderEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player player;
    private final int day;
    private boolean cancelled = false;

    public PlayerReceiveReminderEvent(Player player, int day) {
        this.player = player;
        this.day = day;
    }

    public Player getPlayer() {
        return player;
    }

    public int getDay() {
        return day;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
