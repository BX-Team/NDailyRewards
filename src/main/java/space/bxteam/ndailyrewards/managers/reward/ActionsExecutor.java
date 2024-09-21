package space.bxteam.ndailyrewards.managers.reward;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import space.bxteam.ndailyrewards.utils.LogUtil;
import space.bxteam.ndailyrewards.utils.TextUtils;

import java.util.Random;

public class ActionsExecutor {
    private final Player player;
    private final Reward reward;
    private final String[] titleText = new String[]{"", ""};
    private final Random random = new Random();

    public ActionsExecutor(Player player, Reward reward) {
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
                            LogUtil.log("Invalid sound action: " + action, LogUtil.LogLevel.WARNING);
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
                    if (permParts.length == 2 && player.hasPermission(permParts[0])) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), permParts[1].replace("<player>", player.getName()));
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
                            LogUtil.log("Invalid luck action: " + action, LogUtil.LogLevel.WARNING);
                        }
                    }
                    break;
                case PERMLUCK:
                    if (coloredLine.startsWith("{") && coloredLine.contains("}")) {
                        int endIndex = coloredLine.indexOf("}");
                        String permLuckString = coloredLine.substring(1, endIndex);
                        String[] permLuckParts = permLuckString.split(":");
                        if (permLuckParts.length == 2) {
                            try {
                                String permission = permLuckParts[0];
                                int chance = Integer.parseInt(permLuckParts[1]);
                                String permluckCommand = coloredLine.substring(endIndex + 1).trim();
                                if (player.hasPermission(permission) && random.nextInt(100) < chance) {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), permluckCommand.replace("<player>", player.getName()));
                                }
                            } catch (NumberFormatException e) {
                                LogUtil.log("Invalid permluck action: " + action, LogUtil.LogLevel.WARNING);
                            }
                        }
                    }
                    break;
                case CLOSE:
                    player.closeInventory();
                    break;
            }
        });

        if (!titleText[0].isEmpty() || !titleText[1].isEmpty()) {
            player.sendTitle(titleText[0], titleText[1], 10, 70, 20);
        }
    }
}
