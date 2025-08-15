package org.bxteam.ndailyrewards.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.bxteam.ndailyrewards.NDailyRewards;
import org.bxteam.ndailyrewards.utils.TextUtils;

import java.util.List;
import java.util.stream.Collectors;

public enum Language {
    PREFIX("prefix"),
    NO_PERMISSION("no-permission"),
    NOT_PLAYER("not-player"),
    PLAYER_NOT_FOUND("player-not-found"),
    INVALID_SYNTAX("invalid-syntax"),
    INVALID_DAY("invalid-day"),

    COMMANDS_HELP("commands.help"),
    COMMANDS_RELOAD("commands.reload"),
    COMMANDS_SETDAY("commands.setday"),

    EVENTS_NOTIFY_WHEN_AVAILABLE("events.notify-when-available"),

    CLAIM_ALREADY_CLAIMED("claim.already-claimed"),
    CLAIM_AVAILABLE_SOON("claim.available-soon"),
    CLAIM_NOT_AVAILABLE("claim.not-available"),
    CLAIM_REWARD_RESET("claim.reward-reset");

    private final String path;
    private static FileConfiguration langConfig;

    Language(String path) {
        this.path = path;
    }

    public static void init(@NotNull NDailyRewards plugin) {
        langConfig = plugin.getLangConfig();
    }

    public String asString() {
        return langConfig.getString(this.path);
    }

    public String asColoredString() {
        return TextUtils.applyColor(asString());
    }

    public List<String> asStringList() {
        return langConfig.getStringList(this.path);
    }

    public List<String> asColoredStringList() {
        return asStringList().stream()
                .map(TextUtils::applyColor)
                .collect(Collectors.toList());
    }
}
