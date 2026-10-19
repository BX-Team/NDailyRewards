package org.bxteam.ndailyrewards.configuration;

import org.bxteam.quark.common.QuarkLogger;
import org.bxteam.quark.config.QuarkConfig;
import org.bxteam.quark.config.annotation.Comment;
import org.bxteam.quark.config.annotation.Header;
import org.bxteam.quark.config.annotation.NameStrategy;
import org.bxteam.quark.config.annotation.NameStyle;

import java.nio.file.Path;
import java.util.List;

@Header({
        " -------------------------------------",
        "|    NDailyRewards Messages Config    |",
        " -------------------------------------",
        "You can change all messages in this file, but don't change the keys.",
        "Both MiniMessage tags (<red>, <gradient:...>) and legacy color codes (&a, &#RRGGBB) are supported.",
        "Missing keys are added back with their defaults when the plugin loads."
})
@NameStrategy(NameStyle.HYPHEN_CASE)
public class Messages extends QuarkConfig {
    public String prefix = "&6[&eNDailyRewards&6] &7";
    public String noPermission = "&cYou don't have permission to do that!";
    public String notPlayer = "&cYou must be a player to do that!";
    public String playerNotFound = "&cPlayer not found!";
    public String invalidSyntax = "&cInvalid syntax! Use &f/reward help &cto see available commands";
    public String invalidDay = "&cInvalid day! Specify a number between 1 and <max-day>";

    public Commands commands = new Commands();
    public Events events = new Events();
    public Claim claim = new Claim();
    @Comment("Shown to players with ndailyrewards.update-notify on join and by /reward version")
    public Update update = new Update();

    public static Messages create(Path dataFolder, QuarkLogger logger) {
        return QuarkConfig.create(Messages.class, options -> options
                .file(dataFolder.resolve("lang.yml"))
                .logger(logger));
    }

    public static class Commands {
        public List<String> help = List.of(
                "&6List of available commands:",
                "&f/reward &7- Opens rewards menu",
                "&f/reward claim &7- Claim latest reward without opening the menu",
                "&f/reward help &7- Show this message",
                "&f/reward reload &7- Reloads plugin",
                "&f/reward setday <player> <day> &7- Set player's day",
                "&f/reward version &7- Show plugin version"
        );
        public String reload = "&aPlugin reloaded!";
        @Comment("<error> is the reason, the full report is in the console")
        public String reloadFailed = "<red>Reload failed, the previous settings are kept: <error>";
        public String setday = "&aSet &e<player> &acurrent day to &e<day>";
        public String version = "<green>Current installed version: <yellow><version>";
    }

    public static class Events {
        public String notifyWhenAvailable = "&6You have available rewards! Use &f/reward &6to open the menu";
    }

    public static class Claim {
        public String alreadyClaimed = "&cYou have already claimed this reward";
        public String availableSoon = "&cThis reward will be available soon";
        public String notAvailable = "&cYou can't claim this reward yet";
        public String rewardReset = "&cYour rewards have been reset because you haven't claimed them for a long time";
    }

    public static class Update {
        public String available = "<green>A new update is available: <yellow><version>";
        public String download = "<green>Download here: <yellow><url>";
    }
}
