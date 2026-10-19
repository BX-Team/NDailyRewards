package org.bxteam.ndailyrewards.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Player heads from a UUID, a Mojang texture URL or a base64 texture, for {@code CustomSkull[...]} items.
 * Based on <a href="https://github.com/deanveloper/SkullCreator">SkullCreator</a> by deanveloper.
 */
public final class HeadUtil {
    private HeadUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static ItemStack itemFromUuid(UUID id) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack itemFromUrl(String url) {
        return itemFromBase64(urlToBase64(url));
    }

    public static ItemStack itemFromBase64(String base64) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setPlayerProfile(makeProfile(base64));
        item.setItemMeta(meta);
        return item;
    }

    private static String urlToBase64(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid skin URL: " + url, e);
        }
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + uri + "\"}}}";
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private static PlayerProfile makeProfile(String base64) {
        UUID id = new UUID(
                base64.substring(base64.length() - 20).hashCode(),
                base64.substring(base64.length() - 10).hashCode()
        );
        PlayerProfile profile = Bukkit.createProfile(id, "Player");
        profile.setProperty(new ProfileProperty("textures", base64));
        return profile;
    }
}
