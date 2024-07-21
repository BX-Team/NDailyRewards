package space.bxteam.ndailyrewards.managers.enums;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.utils.TextUtils;

import java.util.List;
import java.util.stream.Collectors;

public enum Language {
    PREFIX("prefix"),
    NO_PERMISSION("no-permission"),
    NOT_PLAYER("not-player"),
    HELP("help"),
    RELOAD("reload"),

    CLAIM_ALREADY_CLAIMED("claim.already-claimed"),
    CLAIM_AVAILABLE_SOON("claim.available-soon"),
    CLAIM_NOT_AVAILABLE("claim.not-available");

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
