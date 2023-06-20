package gq.bxteam.ndailyrewards.manager;

import gq.bxteam.ndailyrewards.manager.objects.Reward;
import org.bukkit.event.inventory.InventoryClickEvent;
import gq.bxteam.ndailyrewards.gui.ContentType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import gq.bxteam.ndailyrewards.utils.ArchUtils;
import org.bukkit.ChatColor;
import gq.bxteam.ndailyrewards.utils.logs.LogUtil;
import gq.bxteam.ndailyrewards.utils.logs.LogType;
import gq.bxteam.ndailyrewards.cfg.Config;
import gq.bxteam.ndailyrewards.manager.objects.DUser;
import gq.bxteam.ndailyrewards.gui.GUIUtils;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashSet;

import gq.bxteam.ndailyrewards.gui.GUIItem;

import java.util.LinkedHashMap;

import gq.bxteam.ndailyrewards.NDailyRewards;
import org.bukkit.entity.Player;

import java.util.Set;

import org.bukkit.inventory.ItemStack;
import gq.bxteam.ndailyrewards.gui.GUI;

public class RewardGUI extends GUI {
    private final int[] day_slots;
    private final ItemStack day_ready;
    private final ItemStack day_taken;
    private final ItemStack day_locked;
    private final ItemStack day_next;
    private Set<Player> opens;

    public RewardGUI(final NDailyRewards plugin, final String title, final int size, final LinkedHashMap<String, GUIItem> items, final int[] day_slots, final ItemStack day_ready, final ItemStack day_taken, final ItemStack day_locked, final ItemStack day_next) {
        super(plugin, title, size, items);
        this.day_slots = day_slots;
        this.day_ready = day_ready;
        this.day_taken = day_taken;
        this.day_locked = day_locked;
        this.day_next = day_next;
        this.start();
    }

    public void start() {
        this.opens = new HashSet<Player>();
        this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {
            @Override
            public void run() {
                for (final Player p : new ArrayList<Player>(RewardGUI.this.opens)) {
                    RewardGUI.this.open(p);
                }
            }
        }, 0L, 20L);
    }

    @Override
    public void open(final Player p) {
        p.openInventory(this.build(p));
    }

    private Inventory build(final Player p) {
        final Inventory inv = this.getInventory();
        final DUser user = this.plugin.getUserManager().getOrLoadUser(p);
        for (final GUIItem gi : this.getContent().values()) {
            int[] slots;
            for (int length = (slots = gi.getSlots()).length, j = 0; j < length; ++j) {
                final int slot = slots[j];
                final ItemStack item = new ItemStack(gi.getItem());
                this.replaceLore(item, user.getDayInRow(), user, user.getNextRewardTime());
                inv.setItem(slot, item);
            }
        }
        final int user_day = user.getDayInRow();
        for (int i = 0; i < this.day_slots.length; ++i) {
            final int day2 = i + 1;
            final int slot2 = this.day_slots[i];
            final long time = user.getNextRewardTime();
            ItemStack icon;
            if (user_day == day2) {
                if (user.hasActiveReward()) {
                    icon = new ItemStack(this.day_ready);
                } else {
                    icon = new ItemStack(this.day_next);
                }
            } else if (user_day > day2) {
                icon = new ItemStack(this.day_taken);
            } else {
                icon = new ItemStack(this.day_locked);
            }
            this.replaceLore(icon, day2, user, time);
            icon = GUIUtils.setPage(icon, day2);
            inv.setItem(slot2, icon);
        }
        return inv;
    }

    private void replaceLore(final ItemStack icon, final int day2, final DUser user, long time) {
        final ItemMeta meta = icon.getItemMeta();
        if (meta.hasDisplayName()) {
            final String n = meta.getDisplayName().replace("%day%", String.valueOf(day2));
            meta.setDisplayName(n);
        }
        if (meta.hasLore()) {
            final Reward rewa = Config.getRewardByDay(day2);
            if (rewa == null) {
                LogUtil.send("&cError! Not found reward for &f" + day2 + "th day!", LogType.ERROR);
                return;
            }
            final List<String> lore = new ArrayList<String>();
            for (String s : meta.getLore()) {
                if (System.currentTimeMillis() > time) {
                    time = System.currentTimeMillis();
                }
                if (s.equalsIgnoreCase("%reward-lore%")) {
                    for (final String s2 : rewa.getLore()) {
                        lore.add(NDailyRewards.replaceHEXColorCode(s2.replace("%day%", String.valueOf(day2))));
                    }
                } else {
                    String pref = NDailyRewards.replaceHEXColorCode(s);
                    lore.add(pref.replace("%expire%", ArchUtils.getTimeLeft(user.getTimeToGetReward())).replace("%time%", ArchUtils.getTimeLeft(time)).replace("%day%", String.valueOf(day2)));
                }
            }
            meta.setLore(lore);
        }
        icon.setItemMeta(meta);
    }

    @EventHandler
    public void onOpen(final InventoryOpenEvent e) {
        if (e.getInventory().getHolder() instanceof RewardGUI) {
            this.opens.add((Player)e.getPlayer());
        }
    }

    @Override
    public boolean onClose(final Player p, final InventoryCloseEvent e) {
        this.opens.remove(p);
        return false;
    }

    @Override
    public boolean click(final Player p, final ItemStack item, final ContentType type, final int slot, final InventoryClickEvent e) {
        if (type == ContentType.EXIT) {
            p.closeInventory();
            return false;
        }
        final int day = GUIUtils.getPage(item);
        final DUser user = this.plugin.getUserManager().getOrLoadUser(p);
        if (user.getDayInRow() == day && user.hasActiveReward()) {
            final Reward r = Config.getRewardByDay(day);
            if (r == null) {
                LogUtil.send("&cError! Not found reward for &f" + day + "th day!", LogType.ERROR);
                return false;
            }
            r.give(p);
            user.takeReward();
            this.open(p);
        }
        return true;
    }
}