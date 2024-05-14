package space.bxteam.ndailyrewards;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public abstract class AbstractListener<P extends Plugin> implements Listener {
    public final P plugin;

    public AbstractListener(final P plugin) {
        this.plugin = plugin;
    }

    public void registerListeners() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    public void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }
}