package org.bxteam.ndailyrewards.manager.reward;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bxteam.ndailyrewards.configuration.SoundEffect;
import org.bxteam.ndailyrewards.messaging.MessageService;
import org.bxteam.quark.common.QuarkLogger;
import org.bxteam.quark.paper.PaperScheduler;

import java.util.List;
import java.util.Random;

/**
 * Runs the action lines of a reward or a custom menu item. Call {@link #execute()} on the thread that owns the
 * player ({@code PaperScheduler#sync(Entity, Runnable)}).
 */
public class ActionsExecutor {
    private final QuarkLogger logger;
    private final MessageService messageService;
    private final PaperScheduler scheduler;
    private final Player player;
    private final List<String> actions;
    private final String[] titleText = new String[]{"", ""};
    private final Random random = new Random();

    @Inject
    public ActionsExecutor(QuarkLogger logger, MessageService messageService, PaperScheduler scheduler,
                           @Assisted Player player, @Assisted List<String> actions) {
        this.logger = logger;
        this.messageService = messageService;
        this.scheduler = scheduler;
        this.player = player;
        this.actions = actions;
    }

    public void execute() {
        actions.forEach(action -> {
            if (action == null || action.isBlank()) return;

            ActionType actionType = ActionType.fromAction(action);
            if (actionType == null) {
                logger.warn("Unknown action type in reward config: '{}'. Expected one of [console], [player], [message], [actionbar], [sound], [title], [subtitle], [permission], [luck], [close] (case-insensitive).", action);
                return;
            }

            String command = actionType.stripPrefix(action);
            String filled = messageService.applyPlaceholders(player, command);

            try {
                switch (actionType) {
                    case CONSOLE:
                        dispatchConsole(filled.replace("<player>", player.getName()));
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
                        try {
                            SoundEffect.parse(filled).play(player);
                        } catch (IllegalArgumentException e) {
                            logger.warn("Invalid sound action '{}': {}", action, e.getMessage());
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
                                dispatchConsole(permParts[1].replace("<player>", player.getName()));
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
                                    dispatchConsole(luckCommand.replace("<player>", player.getName()));
                                }
                            } catch (NumberFormatException e) {
                                logger.warn("Invalid luck action: {}", action);
                            }
                        }
                        break;
                    case CLOSE:
                        player.closeInventory();
                        break;
                }
            } catch (Exception e) {
                logger.error("Error executing action '%s' for player %s".formatted(action, player.getName()), e);
            }
        });

        if (!titleText[0].isEmpty() || !titleText[1].isEmpty()) {
            player.sendTitle(titleText[0], titleText[1], 10, 70, 20);
        }
    }

    private void dispatchConsole(String command) {
        // console commands belong to the global region on Folia; on Paper this is the main thread we are already on
        if (scheduler.isSyncThread()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            scheduler.sync(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        }
    }

    public interface Factory {
        ActionsExecutor create(Player player, List<String> actions);
    }
}
