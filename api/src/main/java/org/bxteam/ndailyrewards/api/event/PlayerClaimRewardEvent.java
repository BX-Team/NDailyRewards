package org.bxteam.ndailyrewards.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player manually claims their daily reward.
 * <p>
 * This event is fired when a player actively claims their daily reward through
 * direct interaction, such as clicking a GUI button or using a command.
 * This is distinct from automatic claiming and represents intentional player action.
 *
 * @since 3.0.0
 * @see AutoClaimEvent
 * @see PlayerReceiveReminderEvent
 */
public class PlayerClaimRewardEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player player;
    private final int day;

    /**
     * Constructs a new PlayerClaimRewardEvent.
     *
     * @param player The player who claimed their daily reward.
     * @param day    The day of the reward that was claimed.
     */
    public PlayerClaimRewardEvent(Player player, int day) {
        this.player = player;
        this.day = day;
    }

    /**
     * Gets the player who claimed their daily reward.
     *
     * @return The player who claimed the reward.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the day of the reward that was claimed.
     *
     * @return The day of the claimed reward.
     */
    public int getDay() {
        return day;
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
