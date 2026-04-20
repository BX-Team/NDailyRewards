package org.bxteam.ndailyrewards.manager.reward;

import java.util.Locale;

public enum ActionType {
    CONSOLE("[console]"),
    PLAYER("[player]"),
    MESSAGE("[message]"),
    ACTIONBAR("[actionbar]"),
    SOUND("[sound]"),
    TITLE("[title]"),
    SUBTITLE("[subtitle]"),
    PERMISSION("[permission]"),
    LUCK("[luck]"),
    CLOSE("[close]");

    private final String prefix;

    ActionType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * Match an action string to a type, case-insensitively.
     * So [Console], [CONSOLE], [console] all map to CONSOLE.
     */
    public static ActionType fromAction(String action) {
        if (action == null) return null;
        String lowered = action.toLowerCase(Locale.ROOT);
        for (ActionType type : values()) {
            if (lowered.startsWith(type.getPrefix())) {
                return type;
            }
        }
        return null;
    }

    /**
     * Strip the action prefix from the line, regardless of casing.
     */
    public String stripPrefix(String action) {
        if (action == null) return "";
        if (action.length() < prefix.length()) return action;
        return action.substring(prefix.length()).trim();
    }
}
