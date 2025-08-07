package org.bxteam.ndailyrewards.event;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.event.Event;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EventCaller {
    private final Server server;

    public <T extends Event> T callEvent(T event) {
        this.server.getPluginManager().callEvent(event);

        return event;
    }
}
