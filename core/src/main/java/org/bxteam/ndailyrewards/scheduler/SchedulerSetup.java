package org.bxteam.ndailyrewards.scheduler;

import com.google.inject.AbstractModule;
import io.papermc.lib.PaperLib;
import io.papermc.lib.environments.Environment;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.bxteam.helix.ServerSoftware;
import org.bxteam.helix.scheduler.Scheduler;
import org.bxteam.helix.scheduler.bukkit.BukkitScheduler;
import org.bxteam.helix.scheduler.folia.FoliaScheduler;
import org.bxteam.helix.scheduler.paper.PaperScheduler;

@RequiredArgsConstructor
public class SchedulerSetup extends AbstractModule {
    private final Plugin plugin;

    @Override
    protected void configure() {
        Environment environment = PaperLib.getEnvironment();

        if (environment.isPaper() && environment.isVersion(20, 3)) {
            this.bind(Scheduler.class).toInstance(new PaperScheduler(this.plugin));
        } else if (ServerSoftware.isFolia()) {
            this.bind(Scheduler.class).toInstance(new FoliaScheduler(this.plugin));
        } else {
            this.bind(Scheduler.class).toInstance(new BukkitScheduler(this.plugin));
        }
    }
}
