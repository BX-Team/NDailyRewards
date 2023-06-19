package gq.bxteam.ndailyrewards.data;

import java.sql.SQLException;

import gq.bxteam.ndailyrewards.NDailyRewards;
import gq.bxteam.ndailyrewards.cfg.Config;
import gq.bxteam.ndailyrewards.utils.logs.LogType;
import gq.bxteam.ndailyrewards.utils.logs.LogUtil;
import org.bukkit.plugin.Plugin;

public class DataManager
{
    private final NDailyRewards plugin;
    private IDataV2 data;
    
    public DataManager(final NDailyRewards plugin) {
        this.plugin = plugin;
    }
    
    public void setup() {
        final DataType d = Config.storage;
        switch (d) {
            case SQLITE: {
                try {
                    this.data = D2SQLite.getInstance();
                    break;
                }
                catch (SQLException e) {
                    LogUtil.send("Unable to connect to " + d.getName() + "!", LogType.ERROR);
                    LogUtil.send(e.getMessage(), LogType.ERROR);
                    this.plugin.getPluginManager().disablePlugin(this.plugin);
                    return;
                }
            }
            case MYSQL: {
                try {
                    this.data = D2MySQL.getInstance();
                }
                catch (SQLException e) {
                    LogUtil.send("Unable to connect to " + d.getName() + "!", LogType.ERROR);
                    LogUtil.send(e.getMessage(), LogType.ERROR);
                    this.plugin.getPluginManager().disablePlugin(this.plugin);
                    return;
                }
                break;
            }
        }
        LogUtil.send("Storage type: &f" + d.getName(), LogType.INFO);
        this.data.open();
        this.data.create();
        this.data.purge();
    }
    
    public void shutdown() {
        this.data.close();
    }
    
    public IDataV2 getData() {
        return this.data;
    }
}
