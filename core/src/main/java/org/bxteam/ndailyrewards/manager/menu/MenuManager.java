package org.bxteam.ndailyrewards.manager.menu;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bxteam.ndailyrewards.configuration.Messages;
import org.bxteam.ndailyrewards.configuration.PluginConfig;
import org.bxteam.ndailyrewards.manager.reward.PlayerRewardData;
import org.bxteam.ndailyrewards.manager.reward.RewardManager;
import org.bxteam.ndailyrewards.messaging.MessageService;
import org.bxteam.ndailyrewards.utils.ItemBuilder;
import org.bxteam.quark.common.QuarkLogger;
import org.bxteam.quark.paper.PaperScheduler;
import org.bxteam.quark.platform.TaskHandle;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class MenuManager {
    private final PluginConfig config;
    private final Messages messages;
    private final RewardManager rewardManager;
    private final PaperScheduler scheduler;
    private final MessageService messageService;
    private final QuarkLogger logger;

    private final InventoryHolder MAIN_MENU_HOLDER = new MainMenuHolder();
    private ItemStack cachedFillerItem;
    private final Map<Integer, DayItems> dayItemsCache = new HashMap<>();
    private final List<CustomItemConfig> cachedCustomItems = new ArrayList<>();
    private final Map<Integer, Integer> slotToDayMap = new HashMap<>();
    private final Set<Player> openMenuPlayers = ConcurrentHashMap.newKeySet();

    private TaskHandle updateTask;
    private boolean itemModelWarningShown;

    @Inject
    public MenuManager(PluginConfig config, Messages messages, RewardManager rewardManager, PaperScheduler scheduler,
                       MessageService messageService, QuarkLogger logger) {
        this.config = config;
        this.messages = messages;
        this.rewardManager = rewardManager;
        this.scheduler = scheduler;
        this.messageService = messageService;
        this.logger = logger;

        initializeCaches();
        startGlobalUpdateTask();
    }

    private void initializeCaches() {
        PluginConfig.RewardMenu menu = config.gui.reward;

        if (menu.display.filler.enable && this.cachedFillerItem == null) {
            this.cachedFillerItem = loadFillItem(menu.display.filler);
        }

        if (cachedCustomItems.isEmpty()) {
            for (PluginConfig.CustomItem custom : menu.custom.values()) {
                ItemStack customItem = new ItemBuilder(parseItem(custom.material))
                        .setName(messageService.toLegacyString(custom.name))
                        .setLore(toLegacyLore(custom.lore))
                        .build();
                cachedCustomItems.add(new CustomItemConfig(custom.position, customItem));
            }
        }

        if (dayItemsCache.isEmpty()) {
            config.rewards.days.forEach((day, settings) -> {
                DayItems dayItems = preCacheDayItems(day, settings);
                dayItemsCache.put(day, dayItems);
                slotToDayMap.put(dayItems.position, day);
            });
        }
    }

    private void startGlobalUpdateTask() {
        updateTask = scheduler.repeating(() -> {
            long currentTime = System.currentTimeMillis() / 1000L;
            for (Player player : openMenuPlayers) {
                updatePlayerInventory(player, currentTime);
            }
        }, Duration.ofSeconds(1), Duration.ofSeconds(1));
    }

    public void openRewardsMenu(Player player) {
        this.rewardManager.checkResetForPlayerAsync(player.getUniqueId())
            .thenCompose(wasReset -> {
                if (wasReset) {
                    scheduler.sync(player, () -> messageService.send(player, messages.claim.rewardReset));
                }

                return this.rewardManager.getPlayerRewardDataAsync(player.getUniqueId());
            })
            .thenAccept(playerRewardData -> {
                PluginConfig.RewardMenu menu = config.gui.reward;
                int size = menu.size;
                String title = messageService.toLegacyString(menu.title);
                boolean useFiller = menu.display.filler.enable;

                scheduler.sync(player, () -> {
                    final Inventory inventory = Bukkit.createInventory(MAIN_MENU_HOLDER, size, title);

                    if (useFiller && cachedFillerItem != null) {
                        for (int i = 0; i < size; i++) {
                            inventory.setItem(i, cachedFillerItem);
                        }
                    }

                    for (CustomItemConfig cic : cachedCustomItems) {
                        inventory.setItem(cic.position, cic.itemStack);
                    }

                    if (playerRewardData != null) {
                        populateDayItems(inventory, playerRewardData, System.currentTimeMillis() / 1000L);
                        openMenuPlayers.add(player);
                    }

                    player.openInventory(inventory);
                });
            });
    }

    private void populateDayItems(Inventory inventory, PlayerRewardData playerRewardData, long currentTime) {
        int currentDay = playerRewardData.currentDay();
        long nextTime = playerRewardData.next();

        for (DayItems cached : dayItemsCache.values()) {
            ItemStack toSet;
            if (currentDay >= cached.day) {
                // Already claimed
                toSet = cached.claimed;
            } else if (currentDay + 1 == cached.day && currentTime >= nextTime) {
                // Available to claim now
                toSet = cached.available;
            } else if (currentDay + 1 == cached.day && currentTime < nextTime) {
                // "Next" day not yet available, update time left
                toSet = updateNextItemTime(cached.nextTemplate, nextTime - currentTime);
            } else {
                // Not available yet, not next, just unavailable
                toSet = cached.unavailable;
            }

            inventory.setItem(cached.position, toSet);
        }
    }

    private void updatePlayerInventory(Player player, long currentTime) {
        if (!player.isOnline()) {
            openMenuPlayers.remove(player);
            return;
        }

        this.rewardManager.getPlayerRewardDataAsync(player.getUniqueId())
            .thenAccept(playerRewardData -> {
                if (playerRewardData != null) {
                    scheduler.sync(player, () -> {
                        Inventory inventory = player.getOpenInventory().getTopInventory();
                        if (!(inventory.getHolder() instanceof MainMenuHolder)) {
                            openMenuPlayers.remove(player);
                            return;
                        }
                        populateDayItems(inventory, playerRewardData, currentTime);
                    });
                }
            });
    }

    private ItemStack loadFillItem(PluginConfig.Filler filler) {
        return new ItemBuilder(parseItem(filler.material))
                .setName(messageService.toLegacyString(filler.name))
                .setLore(toLegacyLore(filler.lore))
                .build();
    }

    private DayItems preCacheDayItems(int day, PluginConfig.Day settings) {
        List<String> rewardLore = toLegacyLore(settings.lore);
        PluginConfig.Display display = config.gui.reward.display;

        ItemStack claimed = createDayItem(display.claimed, day, rewardLore);
        ItemStack available = createDayItem(display.available, day, rewardLore);
        ItemStack unavailable = createDayItem(display.unavailable, day, rewardLore);
        // Build a template for "next" once; we'll just update the time string each tick later
        ItemStack nextTemplate = createDayItem(display.next, day, rewardLore);

        return new DayItems(day, settings.position, claimed, available, unavailable, nextTemplate);
    }

    private ItemStack createDayItem(PluginConfig.DisplayItem item, int day, List<String> rewardLore) {
        String name = Objects.requireNonNullElse(item.name, "").replace("<dayNum>", String.valueOf(day));

        List<String> loreFormatted = item.lore.stream()
                .map(s -> s.replace("<reward-lore>", String.join("\n", rewardLore)))
                .flatMap(s -> Arrays.stream(s.split("\n")))
                .map(messageService::toLegacyString)
                .collect(Collectors.toList());

        return new ItemBuilder(parseItem(item.material))
                .setName(messageService.toLegacyString(name))
                .setLore(loreFormatted)
                .build();
    }

    private ItemStack parseItem(String material) {
        if (!itemModelWarningShown && material.startsWith("ItemModel[") && !ItemBuilder.isItemModelSupported()) {
            itemModelWarningShown = true;
            logger.warn("ItemModel[...] items need Minecraft 1.21.4+; on this server the item model is ignored");
        }
        return ItemBuilder.parseItemStack(material);
    }

    private List<String> toLegacyLore(List<String> lore) {
        return lore.stream()
                .map(messageService::toLegacyString)
                .collect(Collectors.toList());
    }

    private ItemStack updateNextItemTime(ItemStack template, long timeLeft) {
        ItemStack copy = template.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta == null) return copy;

        List<String> lore = meta.getLore();
        if (lore == null) return copy;

        String formattedTime = formatTime(timeLeft);
        List<String> newLore = new ArrayList<>(lore.size());
        for (String line : lore) {
            newLore.add(line.replace("<time-left>", formattedTime));
        }
        meta.setLore(newLore);
        copy.setItemMeta(meta);
        return copy;
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    public void shutdown() {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
        }
        openMenuPlayers.clear();
        dayItemsCache.clear();
        cachedCustomItems.clear();
        slotToDayMap.clear();
        cachedFillerItem = null;
    }

    public void refreshPlayerInventory(Player player) {
        if (!player.isOnline()) {
            openMenuPlayers.remove(player);
            return;
        }

        Inventory inventory = player.getOpenInventory().getTopInventory();
        if (!(inventory.getHolder() instanceof MainMenuHolder)) {
            openMenuPlayers.remove(player);
            return;
        }

        this.rewardManager.getPlayerRewardDataAsync(player.getUniqueId())
            .thenAccept(playerRewardData -> {
                if (playerRewardData != null) {
                    scheduler.sync(player, () -> {
                        long currentTime = System.currentTimeMillis() / 1000L;
                        populateDayItems(inventory, playerRewardData, currentTime);
                    });
                }
            });
    }

    public static class MainMenuHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return Bukkit.createInventory(this, 0, "");
        }
    }

    private record CustomItemConfig(int position, ItemStack itemStack) {}

    private record DayItems(int day, int position, ItemStack claimed, ItemStack available, ItemStack unavailable, ItemStack nextTemplate) {}
}
