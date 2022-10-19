package gq.bxteam.ndailyrewards.data;

import gq.bxteam.ndailyrewards.NDailyRewards;
import gq.bxteam.ndailyrewards.cfg.Config;
import gq.bxteam.ndailyrewards.utils.logs.LogType;
import gq.bxteam.ndailyrewards.utils.logs.LogUtil;

import java.sql.DriverManager;
import java.sql.SQLException;

public class D2MySQL extends IDataV2
{
    private final String url;
    private final String user;
    private final String password;
    private static D2MySQL instance;
    
    static {
        D2MySQL.instance = null;
    }
    
    public D2MySQL(final NDailyRewards plugin) {
        super(plugin);
        this.url = "jdbc:mysql://" + Config.ms_host + "/" + Config.ms_base;
        this.user = Config.ms_login;
        this.password = Config.ms_pass;
    }

    public static synchronized D2MySQL getInstance() throws SQLException {
        if (D2MySQL.instance == null) {
            return new D2MySQL(NDailyRewards.getInstance());
        }
        return D2MySQL.instance;
    }
    
    @Override
    public void open() {
        try {
            this.con = DriverManager.getConnection(this.url, this.user, this.password);
        }
        catch (SQLException e) {
            LogUtil.send(e.getMessage(), LogType.ERROR);
        }
    }
    
    @Override
    public void create() {
        final String sql = "CREATE TABLE IF NOT EXISTS ndailyrewards_data (`id` int(11) NOT NULL AUTO_INCREMENT, `uuid` char(36) CHARACTER SET utf8 NOT NULL, `name` varchar(24) CHARACTER SET utf8 NOT NULL, `login` bigint(64) NOT NULL, `next` bigint(64) NOT NULL, `expire` bigint(64) NOT NULL, `day` int(4) NOT NULL, PRIMARY KEY (`id`));";
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
