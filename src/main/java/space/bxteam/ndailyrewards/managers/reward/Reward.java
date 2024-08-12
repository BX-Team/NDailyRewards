package space.bxteam.ndailyrewards.managers.reward;

import java.util.List;

public class Reward {
    private final List<String> lore;
    private final List<String> actions;

    public Reward(List<String> lore, List<String> actions) {
        this.lore = lore;
        this.actions = actions;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<String> getActions() {
        return actions;
    }
}
