package space.bxteam.ndailyrewards.cfg;

import space.bxteam.ndailyrewards.NDailyRewards;

public class ConfigManager
{
    private final NDailyRewards plugin;
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