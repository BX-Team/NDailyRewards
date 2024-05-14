package space.bxteam.ndailyrewards.data;

import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.utils.logs.LogType;
import space.bxteam.ndailyrewards.utils.logs.LogUtil;

import java.sql.DriverManager;
import org.sqlite.JDBC;
import java.sql.SQLException;

public class D2SQLite extends IDataV2
{
    private static D2SQLite instance;
    
    static {
        D2SQLite.instance = null;
    }

    public static synchronized D2SQLite getInstance() throws SQLException {
        if (D2SQLite.instance == null) {
            return new D2SQLite(NDailyRewards.getInstance());
        }
        return D2SQLite.instance;
    }
    
    private D2SQLite(final NDailyRewards plugin) throws SQLException {
        super(plugin);
        DriverManager.registerDriver(new JDBC());
    }
    
    @Override
    public void open() {
        try {
            this.con = DriverManager.getConnection("jdbc:sqlite:" + this.plugin.getDataFolder().getAbsolutePath() + "/data.db");
        }
        catch (SQLException e) {
            LogUtil.send(e.getMessage(), LogType.ERROR);
        }
    }
    
    @Override
    public void create() {
        final String sql = "CREATE TABLE IF NOT EXISTS ndailyrewards_data ( uuid TEXT NOT NULL,\tname TEXT NOT NULL, login BIGINT NOT NULL, next BIGINT NOT NULL, expire BIGINT NOT NULL, day INTEGER NOT NULL, PRIMARY KEY (uuid));";
        try {
            this.con = this.getConnection();
            (this.ps = this.con.createStatement()).execute(sql);
        }
        catch (SQLException e) {
            LogUtil.send(e.getMessage(), LogType.ERROR);
        }
        finally {
            try {
                this.con.close();
            }
            catch (SQLException ex) {}
            try {
                this.ps.close();
            }
            catch (SQLException ex2) {}
        }
        try {
            this.con.close();
        }
        catch (SQLException ex3) {}
        try {
            this.ps.close();
        }
        catch (SQLException ex4) {}
    }
}
