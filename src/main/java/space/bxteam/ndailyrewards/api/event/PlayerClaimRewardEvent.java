package space.bxteam.ndailyrewards.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called when the player claimed the daily reward manually.
 *
 * @since 3.0.0
 */
public class PlayerClaimRewardEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player player;
    private final int day;

    public PlayerClaimRewardEvent(Player player, int day) {
        this.player = player;
        this.day = day;
    }

    public Player getPlayer() {
        return player;
    }

    public int getDay() {
        return day;
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
