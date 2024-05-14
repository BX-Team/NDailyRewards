package space.bxteam.ndailyrewards.data;

import java.util.ArrayList;
import java.util.List;

import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.cfg.Config;
import space.bxteam.ndailyrewards.utils.logs.LogType;
import space.bxteam.ndailyrewards.utils.logs.LogUtil;
import space.bxteam.ndailyrewards.manager.objects.DUser;

import java.sql.ResultSetMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;

public abstract class IDataV2
{
    protected NDailyRewards plugin;
    protected final String table = "ndailyrewards_data";
    protected Connection con;
    protected Statement ps;
    protected ResultSet rs;
    
    public IDataV2(final NDailyRewards plugin) {
        this.plugin = plugin;
    }
    
    public abstract void create();
    
    public abstract void open();
    
    public Connection getConnection() {
        try {
            if (this.con == null || this.con.isClosed()) {
                this.open();
            }
        }
        catch (SQLException ex) {
            this.open();
        }
        return this.con;
    }
    
    public void close() {
        try {
            if (this.con != null) {
                this.con.close();
            }
            if (this.ps != null) {
                this.ps.close();
            }
            if (this.rs != null) {
                this.rs.close();
            }
        }
        catch (SQLException ex) {}
        finally {
            try {
                if (this.con != null) {
                    this.con.close();
                }
            }
            catch (SQLException ex2) {}
            try {
                if (this.ps != null) {
                    this.ps.close();
                }
            }
            catch (SQLException ex3) {}
            try {
                if (this.rs != null) {
                    this.rs.close();
                }
            }
            catch (SQLException ex4) {}
        }
        try {
            if (this.con != null) {
                this.con.close();
            }
        }
        catch (SQLException ex5) {}
        try {
            if (this.ps != null) {
                this.ps.close();
            }
        }
        catch (SQLException ex6) {}
        try {
            if (this.rs != null) {
                this.rs.close();
            }
        }
        catch (SQLException ex7) {}
    }
    
    public void exec(final String sql) {
        try {
            this.con = this.getConnection();
            this.ps = this.con.prepareStatement(sql);
            ((PreparedStatement)this.ps).executeUpdate();
        }
        catch (SQLException e) {
            LogUtil.send(e.getMessage(), LogType.ERROR);
        }
        finally {
            try {
                if (this.ps != null) {
                    this.ps.close();
                }
            }
            catch (SQLException ex) {}
            try {
                if (this.rs != null) {
                    this.rs.close();
                }
            }
            catch (SQLException ex2) {}
        }
        try {
            if (this.ps != null) {
                this.ps.close();
            }
        }
        catch (SQLException ex3) {}
        try {
            if (this.rs != null) {
                this.rs.close();
            }
        }
        catch (SQLException ex4) {}
    }
    
    protected boolean hasColumn(final String table, final String columnName) {
        try {
            this.con = this.getConnection();
            this.ps = this.con.createStatement();
            final String sql = "SELECT * FROM " + table;
            this.rs = this.ps.executeQuery(sql);
            final ResultSetMetaData rsmd = this.rs.getMetaData();
            for (int columns = rsmd.getColumnCount(), x = 1; x <= columns; ++x) {
                if (columnName.equals(rsmd.getColumnName(x))) {
                    return true;
                }
            }
            return false;
        }
        catch (SQLException e) {
            LogUtil.send(e.getMessage(), LogType.ERROR);
            return false;
        }
        finally {
            try {
                if (this.ps != null) {
                    this.ps.close();
                }
            }
            catch (SQLException ex) {}
            try {
                if (this.rs != null) {
                    this.rs.close();
                }
            }
            catch (SQLException ex2) {}
        }
    }
    
    public void purge() {
        if (!Config.ms_purge) {
            return;
        }
        int i = 0;
        for (final DUser l : this.getUsers()) {
            final long log = l.getLastLogin();
            final long log2 = System.currentTimeMillis() - log;
            final int days = (int)(log2 / 86400000L % 7L);
            if (days >= Config.ms_purge_days) {
                this.del(l.getUUID());
                ++i;
            }
        }
        LogUtil.send("Data purge: Purged &f" + i + " &7inactive users.", LogType.INFO);
    }
    
    public List<DUser> getUsers() {
        final List<DUser> list = new ArrayList<DUser>();
        final String sql = "SELECT * FROM ndailyrewards_data";
        try {
            this.con = this.getConnection();
            this.ps = this.con.createStatement();
            this.rs = this.ps.executeQuery(sql);
            while (this.rs.next()) {
                final String uuid = this.rs.getString("uuid");
                final String name = this.rs.getString("name");
                final long login = this.rs.getLong("login");
                final int day = this.rs.getInt("day");
                final long next = this.rs.getLong("next");
                final long expire = this.rs.getLong("expire");
                list.add(new DUser(uuid, name, login, day, next, expire));
            }
            return list;
        }
        catch (SQLException e) {
            LogUtil.send("Unable to get all players from database!", LogType.ERROR);
            LogUtil.send(e.getMessage(), LogType.ERROR);
            return list;
        }
        finally {
            try {
                if (this.ps != null) {
                    this.ps.close();
                }
            }
            catch (SQLException ex) {}
            try {
                if (this.rs != null) {
                    this.rs.close();
                }
            }
            catch (SQLException ex2) {}
        }
    }
    
    public DUser getByUUID(final String uuid) {
        final String sql = "SELECT * FROM ndailyrewards_data WHERE `uuid` = ?";
        try {
            this.con = this.getConnection();
            this.ps = this.con.prepareStatement(sql);
            ((PreparedStatement)this.ps).setString(1, uuid);
            this.rs = ((PreparedStatement)this.ps).executeQuery();
            if (this.rs.next()) {
                final String uuid2 = this.rs.getString("uuid");
                final String name2 = this.rs.getString("name");
                final long login = this.rs.getLong("login");
                final int day = this.rs.getInt("day");
                final long next = this.rs.getLong("next");
                final long expire = this.rs.getLong("expire");
                return new DUser(uuid2, name2, login, day, next, expire);
            }
            return null;
        }
        catch (SQLException e) {
            LogUtil.send(e.getMessage(), LogType.ERROR);
            return null;
        }
        finally {
            try {
                if (this.ps != null) {
                    this.ps.close();
                }
            }
            catch (SQLException ex) {}
            try {
                if (this.rs != null) {
                    this.rs.close();
                }
            }
            catch (SQLException ex2) {}
        }
    }
    
    public boolean isExists(final String uuid) {
        return this.getByUUID(uuid) != null;
    }
    
    public void save(final DUser mp) {
        final String uuid = mp.getUUID();
        final long date = mp.getLastLogin();
        final int day = mp.getDayInRow();
        final long next = mp.getNextRewardTime();
        final long expire = mp.getTimeToGetReward();
        final String sql = "UPDATE ndailyrewards_data SET `login` = '" + date + "', `next` = '" + next + "', `expire` = '" + expire + "', `day` = '" + day + "'" + " WHERE `uuid` = '" + uuid + "'";
        this.exec(sql);
    }
    
    public void add(final DUser mp) {
        if (this.isExists(mp.getUUID())) {
            return;
        }
        final String uid = mp.getUUID();
        final String name = mp.getName();
        final long date = System.currentTimeMillis();
        final int day = mp.getDayInRow();
        final long next = mp.getNextRewardTime();
        final long expire = mp.getTimeToGetReward();
        final String sql = "INSERT INTO ndailyrewards_data(`uuid`, `name`, `login`, `next`, `expire`, `day`) VALUES('" + uid + "', '" + name + "', '" + date + "', '" + next + "', '" + expire + "', '" + day + "')";
        this.exec(sql);
    }
    
    public void del(final String uuid) {
        final String sql = "DELETE FROM ndailyrewards_data WHERE `uuid` = '" + uuid + "'";
        this.exec(sql);
    }
}
