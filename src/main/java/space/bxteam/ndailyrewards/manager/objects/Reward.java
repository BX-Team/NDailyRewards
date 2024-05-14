package space.bxteam.ndailyrewards.manager.objects;

import space.bxteam.ndailyrewards.utils.ArchUtils;
import space.bxteam.ndailyrewards.utils.TextUtils;
import org.bukkit.entity.Player;

import java.util.List;

public class Reward {
    private final int day;
    private final List<String> lore;
    private final List<String> cmds;
    private final List<String> msg;

    public Reward(final int day, final List<String> lore, final List<String> cmds, final List<String> msg) {
        this.day = day;
        this.lore = lore;
        this.cmds = cmds;
        this.msg = msg;
    }

    public int getDay() {
        return this.day;
    }

    public List<String> getLore() {
        return this.lore;
    }

    public List<String> getCommands() {
        return this.cmds;
    }

    public List<String> getMessages() {
        return this.msg;
    }

    public void give(final Player p) {
        for (final String s : this.cmds) {
            ArchUtils.execCmd(s.replace("%day%", String.valueOf(this.day)), p);
        }
        for (final String s : this.msg) {
            String pref = TextUtils.applyColor(s);
            p.sendMessage(pref.replace("%day%", String.valueOf(this.day)));
        }
    }
}
