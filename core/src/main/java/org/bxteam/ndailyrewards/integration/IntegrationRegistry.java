package org.bxteam.ndailyrewards.integration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bxteam.helix.logger.ExtendedLogger;
import org.bxteam.ndailyrewards.integration.placeholderapi.PlaceholderAPIIntegration;
import org.bxteam.ndailyrewards.manager.reward.RewardManager;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IntegrationRegistry {
    private final PluginManager pluginManager;
    private final PluginDescriptionFile pluginDescription;
    private final ExtendedLogger logger;
    private final RewardManager rewardManager;

    public void init() {
        this.tryEnable("PlaceholderAPI", () -> {
            new PlaceholderAPIIntegration(this.rewardManager, this.pluginDescription).enable();
        });
    }

    private void tryEnable(String pluginName, Integration integration) {
        if (pluginManager.isPluginEnabled(pluginName)) {
            try {
                integration.enable();
                logger.info("Enabled integration " + pluginName);
            } catch (Exception e) {
                logger.error("Failed to enable integration " + pluginName);
                e.printStackTrace();
            }
        }
    }
}
