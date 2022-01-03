package me.nonplay.ndailyrewards.hooks.external;

import org.bukkit.event.EventHandler;
import me.nonplay.ndailyrewards.cfg.Config;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.trait.Trait;

@TraitName("dailyrewards")
public class DailyTrait extends Trait
{
    public DailyTrait() {
        super("dailyrewards");
    }
    
    @EventHandler
    public void click(final NPCRightClickEvent e) {
        if (e.getNPC() == this.getNPC()) {
            Config.rewards_gui.open(e.getClicker());
        }
    }
}
