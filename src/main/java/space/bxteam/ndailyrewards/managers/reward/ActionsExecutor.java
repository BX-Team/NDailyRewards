package space.bxteam.ndailyrewards.managers.reward;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import space.bxteam.ndailyrewards.utils.LogUtil;
import space.bxteam.ndailyrewards.utils.TextUtils;

public class ActionsExecutor {
    private final Player player;
    private final Reward reward;
    private final String[] titleText = new String[]{"", ""};

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
