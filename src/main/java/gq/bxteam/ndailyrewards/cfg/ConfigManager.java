package gq.bxteam.ndailyrewards.cfg;

import gq.bxteam.ndailyrewards.NDailyRewards;

public class ConfigManager
{
    private NDailyRewards plugin;
    public MyConfig configLang;
    public MyConfig configMain;

    public ConfigManager(final NDailyRewards plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        this.configMain = new MyConfig(this.plugin, "", "config.yml");
        this.configLang = new MyConfig(this.plugin, "", "messages.yml");
        Config.setup(this.configMain);
        Lang.setup(this.configLang);
    }
}