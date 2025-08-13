package org.bxteam.ndailyrewards.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player's daily reward is automatically claimed by the system.
 * <p>
 * This event is fired when the auto-claim feature is enabled and a player's
 * daily reward is claimed without direct player interaction. This typically
 * occurs when a player joins the server and has pending rewards that can be
 * automatically claimed based on the plugin's configuration.
 *
 * @since 3.0.0
 * @see PlayerClaimRewardEvent
 * @see PlayerReceiveReminderEvent
 */
public class AutoClaimEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player player;
    private final int day;
    private boolean cancelled = false;

    /**
     * Constructs a new AutoClaimEvent.
     *
     * @param player The player whose daily reward was automatically claimed.
     * @param day    The day of the reward that was automatically claimed.
     */
    public AutoClaimEvent(Player player, int day) {
        this.player = player;
        this.day = day;
    }

    /**
     * Gets the player whose daily reward was automatically claimed.
     *
     * @return The player who received the auto-claimed reward.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the day of the reward that was automatically claimed.
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
