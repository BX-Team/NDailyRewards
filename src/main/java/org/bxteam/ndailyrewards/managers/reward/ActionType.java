package org.bxteam.ndailyrewards.managers.reward;

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

    public static ActionType fromAction(String action) {
        for (ActionType type : values()) {
            if (action.startsWith(type.getPrefix())) {
                return type;
            }
        }
        return null;
    }
}
