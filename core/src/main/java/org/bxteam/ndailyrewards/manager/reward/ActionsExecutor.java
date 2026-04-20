package org.bxteam.ndailyrewards.manager.reward;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bxteam.helix.logger.ExtendedLogger;
import org.bxteam.ndailyrewards.messaging.MessageService;

import java.util.Random;

public class ActionsExecutor {
    private final ExtendedLogger logger;
    private final MessageService messageService;
    private final Player player;
    private final Reward reward;
    private final String[] titleText = new String[]{"", ""};
    private final Random random = new Random();

    @Inject
    public ActionsExecutor(ExtendedLogger logger, MessageService messageService, @Assisted Player player, @Assisted Reward reward) {
        this.logger = logger;
        this.messageService = messageService;
        this.player = player;
        this.reward = reward;
    }

    public void execute() {
        reward.actions().forEach(action -> {
            if (action == null || action.isBlank()) return;

            ActionType actionType = ActionType.fromAction(action);
            if (actionType == null) {
                logger.warn("Unknown action type in reward config: '%s'. Expected one of [console], [player], [message], [actionbar], [sound], [title], [subtitle], [permission], [luck], [close] (case-insensitive).".formatted(action));
                return;
            }

            String command = actionType.stripPrefix(action);
            String filled = messageService.applyPlaceholders(player, command);

            try {
                switch (actionType) {
                    case CONSOLE:
                        String consoleCommand = StringUtils.replace(filled, "<player>", player.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCommand);
                        break;
                    case PLAYER:
                        player.performCommand(filled);
                        break;
                    case MESSAGE:
                        messageService.sendRaw(player, filled);
                        break;
                    case ACTIONBAR:
                        messageService.sendActionBar(player, filled);
                        break;
                    case SOUND:
                        String[] parts = filled.split(":");
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
                        titleText[0] = messageService.toLegacyString(filled);
                        break;
                    case SUBTITLE:
                        titleText[1] = messageService.toLegacyString(filled);
                        break;
                    case PERMISSION:
                        String[] permParts = filled.split(" ", 2);
                        if (permParts.length == 2) {
                            String permission = permParts[0].replace("{", "").replace("}", "");

                            if (player.hasPermission(permission)) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), permParts[1].replace("<player>", player.getName()));
                            }
                        }
                        break;
                    case LUCK:
                        if (filled.startsWith("{") && filled.contains("}")) {
                            int endIndex = filled.indexOf("}");
                            String chanceString = filled.substring(1, endIndex);
                            try {
                                int chance = Integer.parseInt(chanceString);
                                String luckCommand = filled.substring(endIndex + 1).trim();
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
                logger.error("Error executing action '%s' for player %s: %s".formatted(action, player.getName(), e.getMessage()));
                e.printStackTrace();
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
