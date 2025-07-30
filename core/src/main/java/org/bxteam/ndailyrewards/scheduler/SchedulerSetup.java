package org.bxteam.ndailyrewards.scheduler;

import com.google.inject.AbstractModule;
import io.papermc.lib.PaperLib;
import io.papermc.lib.environments.Environment;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.bxteam.commons.bukkit.scheduler.BukkitScheduler;
import org.bxteam.commons.folia.scheduler.FoliaScheduler;
import org.bxteam.commons.paper.scheduler.PaperScheduler;
import org.bxteam.commons.scheduler.Scheduler;

@RequiredArgsConstructor
public class SchedulerSetup extends AbstractModule {
    private final Plugin plugin;
    private static final String FOLIA = "io.papermc.paper.threadedregions.RegionizedServer";

    @Override
    protected void configure() {
        Environment environment = PaperLib.getEnvironment();

        if (environment.isPaper() && environment.isVersion(20, 3)) {
            this.bind(Scheduler.class).toInstance(new PaperScheduler(this.plugin));
        } else if (isFolia()) {
            this.bind(Scheduler.class).toInstance(new FoliaScheduler(this.plugin));
        } else {
            this.bind(Scheduler.class).toInstance(new BukkitScheduler(this.plugin));
        }
    }

    private static boolean isFolia() {
        try {
            Class.forName(FOLIA);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
