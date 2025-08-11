package org.bxteam.ndailyrewards.manager.reward;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bxteam.commons.logger.ExtendedLogger;
import org.bxteam.ndailyrewards.utils.TextUtils;

import java.util.Random;

public class ActionsExecutor {
    private final ExtendedLogger logger;
    private final Player player;
    private final Reward reward;
    private final String[] titleText = new String[]{"", ""};
    private final Random random = new Random();

    @Inject
    public ActionsExecutor(ExtendedLogger logger, @Assisted Player player, @Assisted Reward reward) {
        this.logger = logger;
        this.player = player;
        this.reward = reward;
    }

    public void execute() {
        reward.actions().forEach(action -> {
            ActionType actionType = ActionType.fromAction(action);
            if (actionType == null) return;

            String command = StringUtils.replace(action, actionType.getPrefix(), "").trim();

            String placeholders = TextUtils.applyPlaceholders(player, command);
            String coloredLine = TextUtils.applyColor(placeholders);

            try {
                switch (actionType) {
                    case CONSOLE:
                        String consoleCommand = StringUtils.replace(coloredLine, "<player>", player.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCommand);
                        break;
                    case PLAYER:
                        player.performCommand(coloredLine);
                        break;
                    case MESSAGE:
                        player.sendMessage(coloredLine);
                        break;
                    case ACTIONBAR:
                        player.sendActionBar(coloredLine);
                        break;
                    case SOUND:
                        String[] parts = coloredLine.split(":");
                        if (parts.length == 3) {
                            try {
                                Sound sound = Sound.valueOf(parts[0]);
                                float volume = Float.parseFloat(parts[1]);
                                float pitch = Float.parseFloat(parts[2]);
                                player.playSound(player.getLocation(), sound, volume, pitch);
                            } catch (IllegalArgumentException e) {
                                logger.warn("Invalid sound action: " + action);
                            }
                        }
                        break;
                    case TITLE:
                        titleText[0] = coloredLine;
                        break;
                    case SUBTITLE:
                        titleText[1] = coloredLine;
                        break;
                    case PERMISSION:
                        String[] permParts = coloredLine.split(" ", 2);
                        if (permParts.length == 2) {
                            String permission = permParts[0].replace("{", "").replace("}", "");

                            if (player.hasPermission(permission)) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), permParts[1].replace("<player>", player.getName()));
                            }
                        }
                        break;
                    case LUCK:
                        if (coloredLine.startsWith("{") && coloredLine.contains("}")) {
                            int endIndex = coloredLine.indexOf("}");
                            String chanceString = coloredLine.substring(1, endIndex);
                            try {
                                int chance = Integer.parseInt(chanceString);
                                String luckCommand = coloredLine.substring(endIndex + 1).trim();
                                if (random.nextInt(100) < chance) {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), luckCommand.replace("<player>", player.getName()));
                                }
                            } catch (NumberFormatException e) {
                                logger.warn("Invalid luck action: " + action);
                            }
                        }
                        break;
                    case CLOSE:
                        player.closeInventory();
                        break;
                }
            } catch (Exception e) {
                logger.error("Error executing action: " + e.getMessage());
            }
        });

        if (!titleText[0].isEmpty() || !titleText[1].isEmpty()) {
            player.sendTitle(titleText[0], titleText[1], 10, 70, 20);
        }
    }

    public interface Factory {
        ActionsExecutor create(Player player, Reward reward);
    }
}
