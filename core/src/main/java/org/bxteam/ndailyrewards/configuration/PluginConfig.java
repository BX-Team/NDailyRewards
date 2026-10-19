package org.bxteam.ndailyrewards.configuration;

import org.bxteam.ndailyrewards.database.DatabaseType;
import org.bxteam.quark.common.QuarkLogger;
import org.bxteam.quark.config.ConfigNode;
import org.bxteam.quark.config.Migration;
import org.bxteam.quark.config.QuarkConfig;
import org.bxteam.quark.config.annotation.Comment;
import org.bxteam.quark.config.annotation.ConfigVersion;
import org.bxteam.quark.config.annotation.Header;
import org.bxteam.quark.config.annotation.NameStrategy;
import org.bxteam.quark.config.annotation.NameStyle;
import org.bxteam.quark.config.serdes.TypeSerializer;
import org.bxteam.quark.config.validator.Check;
import org.bxteam.quark.config.validator.Max;
import org.bxteam.quark.config.validator.Min;
import org.bxteam.quark.config.validator.NotNull;

import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Header({
        "███╗░░██╗██████╗░░█████╗░██╗██╗░░░░░██╗░░░██╗██████╗░███████╗░██╗░░░░░░░██╗░█████╗░██████╗░██████╗░░██████╗",
        "████╗░██║██╔══██╗██╔══██╗██║██║░░░░░╚██╗░██╔╝██╔══██╗██╔════╝░██║░░██╗░░██║██╔══██╗██╔══██╗██╔══██╗██╔════╝",
        "██╔██╗██║██║░░██║███████║██║██║░░░░░░╚████╔╝░██████╔╝█████╗░░░╚██╗████╗██╔╝███████║██████╔╝██║░░██║╚█████╗░",
        "██║╚████║██║░░██║██╔══██║██║██║░░░░░░░╚██╔╝░░██╔══██╗██╔══╝░░░░████╔═████║░██╔══██║██╔══██╗██║░░██║░╚═══██╗",
        "██║░╚███║██████╔╝██║░░██║██║███████╗░░░██║░░░██║░░██║███████╗░░╚██╔╝░╚██╔╝░██║░░██║██║░░██║██████╔╝██████╔╝",
        "╚═╝░░╚══╝╚═════╝░╚═╝░░╚═╝╚═╝╚══════╝░░░╚═╝░░░╚═╝░░╚═╝╚══════╝░░░╚═╝░░░╚═╝░░╚═╝░░╚═╝╚═╝░░╚═╝╚═════╝░╚═════╝░",
        "",
        "Discord server: https://discord.gg/qNyybSSPm5",
        "Modrinth: https://modrinth.com/plugin/ndailyrewards"
})
@ConfigVersion(2)
@NameStrategy(NameStyle.HYPHEN_CASE)
public class PluginConfig extends QuarkConfig {
    @Comment("Check Modrinth for a new version on start and tell admins about it when they join")
    public boolean checkUpdates = true;

    @Comment({
            "Enable verbose debug logging. Useful for diagnosing reward actions,",
            "database issues, and placeholder behavior. Leave off in production."
    })
    public boolean debug = false;

    @Comment("Database configuration")
    public Database database = new Database();

    @Comment("Player events")
    public Events events = new Events();

    @Comment("Rewards configuration")
    public Rewards rewards = new Rewards();

    public Gui gui = new Gui();

    public Sounds sound = new Sounds();

    public static PluginConfig create(Path dataFolder, QuarkLogger logger) {
        return QuarkConfig.create(PluginConfig.class, options -> options
                .file(dataFolder.resolve("config.yml"))
                .serdes(registry -> registry.register(SoundEffect.class, TypeSerializer.ofString(SoundEffect::parse, SoundEffect::toString)))
                .migrations(Migration.of(1, 2, PluginConfig::migrateFromBukkitConfig))
                .logger(logger));
    }

    private static void migrateFromBukkitConfig(ConfigNode root) {
        ConfigNode custom = root.node("gui", "reward", "custom");
        if (custom.isList()) {
            List<ConfigNode> items = new ArrayList<>();
            custom.elements().forEach(item -> items.add(item.copy()));
            custom.setMap();
            for (int i = 0; i < items.size(); i++) {
                custom.node(String.valueOf(i + 1)).set(items.get(i));
            }
        }

        ConfigNode database = root.node("database");
        for (String property : Database.defaultDataSourceProperties().keySet()) {
            ConfigNode value = database.node(property);
            if (!value.isVirtual()) {
                value.moveTo(database.node("data-source-properties", property));
            }
        }
    }

    public static class Database {
        @Comment({
                "Select here the database you want to use",
                "The following databases are supported:",
                " - SQLITE - (default) stores all data in a local file",
                " - MARIADB - allows using a remote database"
        })
        @NotNull
        public DatabaseType type = DatabaseType.SQLITE;

        @Comment("SQLite configuration")
        public Sqlite sqlite = new Sqlite();

        @Comment("MariaDB configuration")
        public MariaDb mariadb = new MariaDb();

        @Comment({
                "Advanced HikariCP data source properties for MariaDB (do not touch unless you know what you are doing)",
                "Remove a property to stop setting it."
        })
        public Map<String, Object> dataSourceProperties = defaultDataSourceProperties();

        static Map<String, Object> defaultDataSourceProperties() {
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("cachePrepStmts", true);
            properties.put("prepStmtCacheSize", 250);
            properties.put("prepStmtCacheSqlLimit", 2048);
            properties.put("useServerPrepStmts", true);
            properties.put("useLocalSessionState", true);
            properties.put("rewriteBatchedStatements", true);
            properties.put("cacheResultSetMetadata", true);
            properties.put("cacheServerConfiguration", true);
            properties.put("elideSetAutoCommits", true);
            properties.put("maintainTimeStats", false);
            return properties;
        }
    }

    public static class Sqlite {
        @NotNull
        public String file = "ndailyrewards.db";
    }

    public static class MariaDb {
        @NotNull
        public String jdbc = "jdbc:mariadb://localhost:3306/ndailyrewards";
        public String username = "root";
        public String password = "password";
    }

    public static class Events {
        @Comment("Whether a reward should automatically claim when a player joins (if available)")
        public boolean autoClaimReward = false;

        @Comment("Delay in seconds before the reward is automatically claimed")
        @Min(0)
        public int autoClaimDelay = 10;

        @Comment("When player joins the server, should plugin open the reward GUI?")
        public boolean openGuiWhenAvailable = false;

        @Comment("When player joins the server, should plugin notify player about available rewards?")
        public boolean notifyWhenAvailable = true;
    }

    public static class Rewards {
        @Comment("When a player reaches the day limit, should it reset to the first day?")
        public boolean resetWhenAllClaimed = true;

        @Comment("When player joins server for the first time, should the reward be available?")
        public boolean firstJoinReward = false;

        @Comment("When all rewards claimed and days has been reset, should the first day be available immediately?")
        public boolean firstDayOnReset = false;

        @Comment("Time the player has to wait before claiming the next reward (in hours)")
        @Min(0)
        public int cooldown = 24;

        @Comment("Time without claiming a reward after which the player's streak resets to day 1 (in hours)")
        @Min(0)
        public int resetTime = 24;

        @Comment("Should next day unlock after midnight?")
        public boolean unlockAfterMidnight = false;

        @Comment({
                "Timezone for `unlock-after-midnight` and \"has claimed today\" checks.",
                "Use \"system\" to use the server machine's JVM default, or a TZ database name.",
                "Examples: \"UTC\", \"Europe/Moscow\", \"America/New_York\", \"Asia/Tokyo\""
        })
        @Check(value = ValidTimezone.class, message = "must be \"system\" or a TZ database name such as UTC or Europe/Moscow")
        public String timezone = "system";

        @Comment({
                "Available actions:",
                "[console] - entered statement will be executed from console",
                "[player] - statement performed as player command execution",
                "[message] - messages the player with specified text",
                "[actionbar] - shows the text in actionbar for the player",
                "[title] - sends primary title with entered text",
                "[subtitle] - sends secondary title with entered text",
                "[sound] - plays specified sound for the player (sound:volume:pitch)",
                "[permission] {ndailyrewards.permission} - checks if player has permission",
                "[luck] {0-100} - executes the statement with a specified chance (0-100)",
                "[close] - closes the inventory for the player",
                "You can also use PlaceholderAPI placeholders (requires PlaceholderAPI plugin)",
                "Note: First slot in inventory is 0, not 1"
        })
        public Map<Integer, Day> days = defaultDays();

        public ZoneId zoneId() {
            return ValidTimezone.isSystem(timezone) ? ZoneId.systemDefault() : ZoneId.of(timezone);
        }

        private static Map<Integer, Day> defaultDays() {
            Map<Integer, Day> days = new LinkedHashMap<>();
            for (int day = 1; day <= 7; day++) {
                days.put(day, new Day(9 + day,
                        List.of("&7&lRewards:", "&6- &e%dx &6Diamond".formatted(day)),
                        List.of("[console] give <player> diamond " + day,
                                "[message] &6You have claimed your reward!",
                                "[sound] ENTITY_EXPERIENCE_ORB_PICKUP:1:1")));
            }
            return days;
        }
    }

    public static class Day {
        @Min(0)
        @Max(53)
        public int position;
        public List<String> lore = List.of();
        public List<String> actions = List.of();

        private Day() {
        }

        Day(int position, List<String> lore, List<String> actions) {
            this.position = position;
            this.lore = lore;
            this.actions = actions;
        }
    }

    public static class Gui {
        public RewardMenu reward = new RewardMenu();
    }

    public static class RewardMenu {
        public String title = "&6Daily Rewards";

        @Min(9)
        @Max(54)
        @Check(value = MultipleOfNine.class, message = "must be a multiple of 9")
        public int size = 27;

        public Display display = new Display();

        @Comment("Custom GUI items")
        public Map<String, CustomItem> custom = new LinkedHashMap<>();
    }

    public static class Display {
        public DisplayItem available = new DisplayItem("EMERALD_BLOCK:1", "&aDay <dayNum>",
                List.of("&7Your Reward Awaits", "&7Click Me to claim your prize!", "", "<reward-lore>"));
        public DisplayItem claimed = new DisplayItem("COAL_BLOCK:1", "&aDay <dayNum>",
                List.of("&7You have claimed this reward", "", "<reward-lore>"));
        public DisplayItem next = new DisplayItem("COAL_BLOCK:1", "&aDay <dayNum>",
                List.of("&7Your Reward Will Be Here Soon", "&7Wait <time-left>", "", "<reward-lore>"));
        public DisplayItem unavailable = new DisplayItem("BARRIER:1", "&aDay <dayNum>",
                List.of("&7You have not reached this day yet", "", "<reward-lore>"));
        public Filler filler = new Filler();
    }

    public static class DisplayItem {
        @NotNull
        public String material = "STONE:1";
        public String name = "";
        public List<String> lore = List.of();

        private DisplayItem() {
        }

        DisplayItem(String material, String name, List<String> lore) {
            this.material = material;
            this.name = name;
            this.lore = lore;
        }
    }

    public static class Filler {
        public boolean enable = true;
        @NotNull
        public String material = "GRAY_STAINED_GLASS_PANE:1";
        public String name = "&7";
        public List<String> lore = List.of();
    }

    public static class CustomItem {
        @Min(0)
        @Max(53)
        public int position;
        @NotNull
        public String material = "STONE:1";
        public String name = "";
        public List<String> lore = List.of();
        public List<String> actions = List.of();
    }

    public static class Sounds {
        public SoundSetting open = new SoundSetting();
    }

    public static class SoundSetting {
        public boolean enabled = true;
        @NotNull
        public SoundEffect type = SoundEffect.parse("BLOCK_BARREL_OPEN:1:1");
    }

    public static final class ValidTimezone implements Predicate<String> {
        static boolean isSystem(String timezone) {
            return timezone == null || timezone.isBlank() || timezone.equalsIgnoreCase("system") || timezone.equalsIgnoreCase("default");
        }

        @Override
        public boolean test(String timezone) {
            if (isSystem(timezone)) {
                return true;
            }
            try {
                ZoneId.of(timezone);
                return true;
            } catch (DateTimeException e) {
                return false;
            }
        }
    }

    public static final class MultipleOfNine implements Predicate<Integer> {
        @Override
        public boolean test(Integer value) {
            return value == null || value % 9 == 0;
        }
    }
}
