package org.bxteam.ndailyrewards.messaging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bxteam.ndailyrewards.configuration.Language;

import java.util.HashMap;
import java.util.Map;

/**
 * Sends messages supporting both legacy ampersand codes (&a, &c, etc.) and MiniMessage tags
 * (<red>, <gradient:#ff0000:#00ff00>, <click>, ...).
 * Ampersand codes are converted to MiniMessage before parsing, so both styles can be mixed.
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessageService {
    private final Plugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacyAmpersand = LegacyComponentSerializer.legacyAmpersand();
    private final LegacyComponentSerializer legacySection = LegacyComponentSerializer.legacySection();
    private BukkitAudiences audiences;

    public void init() {
        this.audiences = BukkitAudiences.create(plugin);
    }

    public void shutdown() {
        if (audiences != null) {
            audiences.close();
            audiences = null;
        }
    }

    public void send(CommandSender sender, Language key, String... placeholders) {
        sendRaw(sender, Language.PREFIX.asString() + key.asString(), placeholders);
    }

    public void sendPrefixed(CommandSender sender, String raw, String... placeholders) {
        sendRaw(sender, Language.PREFIX.asString() + raw, placeholders);
    }

    public void sendList(CommandSender sender, Language key, String... placeholders) {
        for (String line : key.asStringList()) {
            sendRaw(sender, line, placeholders);
        }
    }

    public void sendRaw(CommandSender sender, String raw, String... placeholders) {
        if (sender == null || raw == null) return;
        audience(sender).sendMessage(format(raw, toMap(placeholders)));
    }

    public void sendActionBar(CommandSender sender, String raw, String... placeholders) {
        if (sender == null || raw == null) return;
        audience(sender).sendActionBar(format(raw, toMap(placeholders)));
    }

    public Component format(String raw, String... placeholders) {
        return format(raw, toMap(placeholders));
    }

    public Component format(String raw, Map<String, String> placeholders) {
        String replaced = replacePlaceholders(raw, placeholders);
        try {
            String prepared = convertAmpersand(replaced);
            return miniMessage.deserialize(prepared);
        } catch (Exception e) {
            return legacyAmpersand.deserialize(replaced);
        }
    }

    /**
     * Substitutes PlaceholderAPI placeholders in the given text for the provided player.
     * Returns the input unchanged if PlaceholderAPI is not installed or player is null.
     */
    public String applyPlaceholders(Player player, String text) {
        if (text == null) return null;
        if (player != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    /**
     * Serialize a parsed MiniMessage/legacy string into a §-prefixed legacy string,
     * for APIs that still require plain text (ItemMeta#setDisplayName, setLore, inventory titles, sendTitle).
     */
    public String toLegacyString(String raw) {
        if (raw == null) return null;
        try {
            return legacySection.serialize(format(raw));
        } catch (Exception e) {
            return raw;
        }
    }

    private net.kyori.adventure.audience.Audience audience(CommandSender sender) {
        if (audiences == null) init();
        return audiences.sender(sender);
    }

    private static Map<String, String> toMap(String[] pairs) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < pairs.length - 1; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }

    private static String replacePlaceholders(String input, Map<String, String> placeholders) {
        if (placeholders == null || placeholders.isEmpty()) return input;
        String out = input;
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            out = out.replace(e.getKey(), e.getValue() == null ? "" : e.getValue());
        }
        return out;
    }

    /**
     * Converts all standard ampersand color/format codes into their MiniMessage equivalents
     * so a single MiniMessage.deserialize pass handles both styles.
     * Hex `&#RRGGBB` is also supported.
     */
    public static String convertAmpersand(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder out = new StringBuilder(input.length() + 16);
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if ((c == '&' || c == '\u00A7') && i + 1 < input.length()) {
                char next = input.charAt(i + 1);
                if (next == '#' && i + 7 < input.length()) {
                    String hex = input.substring(i + 2, i + 8);
                    if (hex.matches("[0-9A-Fa-f]{6}")) {
                        out.append("<#").append(hex).append('>');
                        i += 8;
                        continue;
                    }
                }
                String tag = switch (Character.toLowerCase(next)) {
                    case '0' -> "<black>";
                    case '1' -> "<dark_blue>";
                    case '2' -> "<dark_green>";
                    case '3' -> "<dark_aqua>";
                    case '4' -> "<dark_red>";
                    case '5' -> "<dark_purple>";
                    case '6' -> "<gold>";
                    case '7' -> "<gray>";
                    case '8' -> "<dark_gray>";
                    case '9' -> "<blue>";
                    case 'a' -> "<green>";
                    case 'b' -> "<aqua>";
                    case 'c' -> "<red>";
                    case 'd' -> "<light_purple>";
                    case 'e' -> "<yellow>";
                    case 'f' -> "<white>";
                    case 'k' -> "<obfuscated>";
                    case 'l' -> "<bold>";
                    case 'm' -> "<strikethrough>";
                    case 'n' -> "<underlined>";
                    case 'o' -> "<italic>";
                    case 'r' -> "<reset>";
                    default -> null;
                };
                if (tag != null) {
                    out.append(tag);
                    i += 2;
                    continue;
                }
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }
}
