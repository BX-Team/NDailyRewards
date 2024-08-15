package space.bxteam.ndailyrewards.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.managers.reward.PlayerRewardData;
import space.bxteam.ndailyrewards.managers.reward.RewardManager;
import space.bxteam.ndailyrewards.utils.ItemBuilder;
import space.bxteam.ndailyrewards.utils.TextUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class MenuManager {
    private final InventoryHolder MAIN_MENU_HOLDER = new MainMenuHolder();

    public void openRewardsMenu(Player player) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(NDailyRewards.getInstance(), () -> {
            final Inventory inventory = Bukkit.createInventory(
                    MAIN_MENU_HOLDER,
                    NDailyRewards.getInstance().getConfig().getInt("gui.reward.size"),
                    TextUtils.applyColor(NDailyRewards.getInstance().getConfig().getString("gui.reward.title")));

            if (NDailyRewards.getInstance().getConfig().getBoolean("gui.reward.other.filler.enable")) {
                for (int i = 0; i < NDailyRewards.getInstance().getConfig().getInt("gui.reward.size"); i++) {
                    inventory.setItem(i, loadFillItem());
                }
            }

            ConfigurationSection daysSection = NDailyRewards.getInstance().getConfig().getConfigurationSection("rewards.days");
            if (daysSection != null) {
                RewardManager rewardManager = NDailyRewards.getInstance().getRewardManager();
                PlayerRewardData playerRewardData = rewardManager.getPlayerRewardData(player.getUniqueId());
                final AtomicReference<BukkitTask> task = new AtomicReference<>();

                task.set(Bukkit.getScheduler().runTaskTimer(NDailyRewards.getInstance(), () -> {
                    if (!(inventory.getHolder() instanceof MainMenuHolder)) {
                        task.get().cancel();
                        return;
                    }

                    for (String dayKey : daysSection.getKeys(false)) {
                        int day = Integer.parseInt(dayKey);
                        ConfigurationSection daySection = daysSection.getConfigurationSection(dayKey);
                        if (daySection != null) {
                            int position = daySection.getInt("position");

                            ItemStack rewardItem;
                            if (checkIfClaimed(playerRewardData, day)) {
                                rewardItem = createItemStack("claimed", day, daySection);
                            } else if (checkIfAvailable(playerRewardData, day)) {
                                rewardItem = createItemStack("available", day, daySection);
                            } else if (checkIfNext(playerRewardData, day)) {
                                long timeLeft = playerRewardData.next() - System.currentTimeMillis() / 1000L;
                                String timeLeftFormatted = formatTime(timeLeft);
                                String material = NDailyRewards.getInstance().getConfig().getString("gui.reward.display.next.material");
                                String name = NDailyRewards.getInstance().getConfig().getString("gui.reward.display.next.name").replace("<dayNum>", String.valueOf(day));
                                List<String> rewardLore = daySection.getStringList("lore").stream()
                                        .map(TextUtils::applyColor)
                                        .collect(Collectors.toList());
                                String rewardLoreJoined = String.join("\n", rewardLore);
                                List<String> loreFormatted = NDailyRewards.getInstance().getConfig().getStringList("gui.reward.display.next.lore").stream()
                                        .map(s -> s.replace("<reward-lore>", rewardLoreJoined).replace("<time-left>", timeLeftFormatted))
                                        .flatMap(s -> Arrays.stream(s.split("\n")))
                                        .collect(Collectors.toList());

                                rewardItem = new ItemBuilder(ItemBuilder.parseItemStack(Objects.requireNonNull(material)))
                                        .setName(name)
                                        .setLore(loreFormatted)
                                        .build();
                            } else {
                                rewardItem = createItemStack("unavailable", day, daySection);
                            }

                            inventory.setItem(position, rewardItem);
                        }
                    }
                }, 0L, 20L));
            }

            player.openInventory(inventory);
        });
    }

    private ItemStack loadFillItem() {
        String material = NDailyRewards.getInstance().getConfig().getString("gui.reward.other.filler.material");
        String name = NDailyRewards.getInstance().getConfig().getString("gui.reward.other.filler.name");
        List<String> lore = NDailyRewards.getInstance().getConfig().getStringList("gui.reward.other.filler.lore");

        return new ItemBuilder(ItemBuilder.parseItemStack(Objects.requireNonNull(material)))
                .setName(name)
                .setLore(lore)
                .build();
    }

    private boolean checkIfClaimed(PlayerRewardData playerRewardData, int day) {
        return playerRewardData.currentDay() >= day;
    }

    private boolean checkIfAvailable(PlayerRewardData playerRewardData, int day) {
        return playerRewardData.currentDay() + 1 == day && System.currentTimeMillis() / 1000L >= playerRewardData.next();
    }

    private boolean checkIfNext(PlayerRewardData playerRewardData, int day) {
        return playerRewardData.currentDay() + 1 == day && System.currentTimeMillis() / 1000L < playerRewardData.next();
    }

    private ItemStack createItemStack(String type, int day, ConfigurationSection daySection) {
        String material = NDailyRewards.getInstance().getConfig().getString("gui.reward.display." + type + ".material");
        String name = NDailyRewards.getInstance().getConfig().getString("gui.reward.display." + type + ".name").replace("<dayNum>", String.valueOf(day));

        List<String> rewardLore = daySection.getStringList("lore").stream()
                .map(TextUtils::applyColor)
                .collect(Collectors.toList());
        String rewardLoreJoined = String.join("\n", rewardLore);

        List<String> loreFormatted = NDailyRewards.getInstance().getConfig().getStringList("gui.reward.display." + type + ".lore").stream()
                .map(s -> s.replace("<reward-lore>", rewardLoreJoined))
                .flatMap(s -> Arrays.stream(s.split("\n")))
                .collect(Collectors.toList());

        return new ItemBuilder(ItemBuilder.parseItemStack(Objects.requireNonNull(material)))
                .setName(name)
                .setLore(loreFormatted)
                .build();
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    public static class MainMenuHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }
    }
}
