package org.bxteam.ndailyrewards.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player receives a reminder notification for their unclaimed daily reward.
 * <p>
 * This event is fired when the plugin sends a reminder to a player about their
 * available daily reward. Reminders can be triggered by various actions such as
 * player login, periodic checks, or specific plugin configurations. The reminder
 * appears as a message.
 *
 * @since 3.0.0
 * @see AutoClaimEvent
 * @see PlayerClaimRewardEvent
 */
public class PlayerReceiveReminderEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player player;
    private final int day;
    private boolean cancelled = false;

    /**
     * Constructs a new PlayerReceiveReminderEvent.
     *
     * @param player The player who received the reminder notification.
     * @param day    The day of the reward for which the reminder was sent.
     */
    public PlayerReceiveReminderEvent(Player player, int day) {
        this.player = player;
        this.day = day;
    }

    /**
     * Gets the player who received the reminder notification.
     *
     * @return The player who received the reminder.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the day of the reward for which the reminder was sent.
     *
     * @return The day of the reward.
     */
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

    /**
     * Gets the handler list for this event.
     *
     * @return The handler list for this event.
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
