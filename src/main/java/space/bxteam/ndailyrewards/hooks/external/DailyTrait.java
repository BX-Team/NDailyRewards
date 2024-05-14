package space.bxteam.ndailyrewards.hooks.external;

import space.bxteam.ndailyrewards.cfg.Config;
import org.bukkit.event.EventHandler;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.trait.Trait;

@TraitName("ndailyrewards")
@Deprecated(since = "1.7.1")
public class DailyTrait extends Trait
{
    public DailyTrait() {
        super("ndailyrewards");
    }
    
    @EventHandler
    public void click(final NPCRightClickEvent e) {
        if (e.getNPC() == this.getNPC()) {
            Config.rewards_gui.open(e.getClicker());
        }
    }
}
