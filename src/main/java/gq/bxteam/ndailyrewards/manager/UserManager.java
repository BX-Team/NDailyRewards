package gq.bxteam.ndailyrewards.manager;

import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collection;

import org.bukkit.scheduler.BukkitRunnable;
import gq.bxteam.ndailyrewards.data.DataType;
import gq.bxteam.ndailyrewards.cfg.Config;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.plugin.Plugin;

import java.util.Set;

import gq.bxteam.ndailyrewards.manager.objects.DUser;

import java.util.Map;

import gq.bxteam.ndailyrewards.NDailyRewards;
import gq.bxteam.ndailyrewards.AbstractListener;

public class UserManager extends AbstractListener<NDailyRewards> {
    private Map<String, DUser> users;
    private final Set<DUser> save;

    public UserManager(final NDailyRewards plugin) {
        super(plugin);
        this.save = new HashSet<DUser>();
    }

    public void setup() {
        this.users = new HashMap<String, DUser>();
        for (final Player p : this.plugin.getServer().getOnlinePlayers()) {
            this.getOrLoadUser(p);
        }
        this.registerListeners();
    }

    public void shutdown() {
        this.autosave();
        this.users.clear();
        this.save.clear();
        this.unregisterListeners();
    }

    public void autosave() {
        for (final DUser cu : this.save) {
            this.plugin.getData().save(cu);
        }
        this.save.clear();
        for (final DUser cu : this.getUsers()) {
            this.plugin.getData().save(cu);
            this.users.put(cu.getUUID(), cu);
        }
    }

    public DUser getOrLoadUser(final Player p) {
        final String uuid = p.getUniqueId().toString();
        if (this.users.containsKey(uuid)) {
            return this.users.get(uuid);
        }
        for (final DUser cu : this.save) {
            if (cu.getUUID().equalsIgnoreCase(uuid)) {
                this.save.remove(cu);
                this.users.put(uuid, cu);
                return cu;
            }
        }
        DUser user = this.plugin.getData().getByUUID(uuid);
        if (user != null) {
            this.users.put(uuid, user);
            return user;
        }
        user = new DUser(p);
        this.plugin.getData().add(user);
        this.users.put(uuid, user);
        return user;
    }

    public void unloadUser(final Player p) {
        final String ui = p.getUniqueId().toString();
        if (this.users.containsKey(ui)) {
            final DUser user = this.users.get(ui);
            user.setLastLogin(System.currentTimeMillis());
            if (Config.storage == DataType.MYSQL) {
                new BukkitRunnable() {
                    public void run() {
                        UserManager.this.plugin.getData().save(user);
                    }
                }.runTaskAsynchronously(this.plugin);
            } else {
                this.save.add(user);
            }
            this.users.remove(ui);
        }
    }

    public Collection<DUser> getUsers() {
        return this.users.values();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        final DUser user = this.getOrLoadUser(p);
        user.updateRewards();
        if (Config.opt_auto_is && (!Config.opt_auto_have || (Config.opt_auto_have && user.hasActiveReward()))) {
            new BukkitRunnable() {
                public void run() {
                    if (p.hasPermission("ndailyrewards.openonjoin")) Config.rewards_gui.open(p);
                }
            }.runTaskLater(this.plugin, 10L);
        }
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        final Player p = e.getPlayer();
        this.unloadUser(p);
    }
}