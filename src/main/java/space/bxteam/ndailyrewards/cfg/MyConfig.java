package space.bxteam.ndailyrewards.cfg;

import java.io.IOException;

import space.bxteam.ndailyrewards.utils.Files;

import java.io.File;
import space.bxteam.ndailyrewards.NDailyRewards;

public class MyConfig
{
    private final NDailyRewards plugin;
    private final String name;
    private final String path;
    private JYML fileConfiguration;
    private File file;

    public MyConfig(final NDailyRewards plugin, final String path, final String name) {
        this.plugin = plugin;
        this.name = name;
        this.path = path;
        this.load();
    }

    private void load() {
        if (!this.plugin.getDataFolder().exists()) {
            Files.mkdir(this.plugin.getDataFolder());
        }
        final File folder = new File(this.plugin.getDataFolder() + "/" + this.path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        final File file = new File(this.plugin.getDataFolder() + "/" + this.path, this.name);
        if (!file.exists()) {
            Files.copy(NDailyRewards.class.getResourceAsStream(this.path + "/" + this.name), file);
        }
        this.file = file;
        this.fileConfiguration = new JYML(file);
        this.fileConfiguration.options().copyDefaults(true);
    }

    public void save() {
        try {
            this.fileConfiguration.options().copyDefaults(true);
            this.fileConfiguration.save(this.file);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public JYML getConfig() {
        return this.fileConfiguration;
    }
}