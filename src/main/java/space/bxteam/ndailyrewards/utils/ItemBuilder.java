package space.bxteam.ndailyrewards.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.stream.Collectors;

public class ItemBuilder {
    private final ItemStack itemStack;
    private final ItemMeta meta;

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.meta = itemStack.getItemMeta();
    }

    /**
     * Parse an item stack from a string
     *
     * @param materialString The string to parse (MATERIAL:QUANTITY)
     * @return The parsed item stack
     */
    public static ItemStack parseItemStack(String materialString) {
        String[] parts = materialString.split(":");
        Material material = Material.valueOf(parts[0]);
        int quantity = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
        return new ItemStack(material, quantity);
    }

    /**
     * Set the name of the item
     *
     * @param name The name of the item
     * @return The ItemBuilder instance
     */
    public ItemBuilder setName(final String name) {
        this.meta.setDisplayName(TextUtils.applyColor(name));
        return this;
    }

    /**
     * Set the lore of the item
     *
     * @param lore The lore of the item
     * @return The ItemBuilder instance
     */
    public ItemBuilder setLore(final List<String> lore) {
        List<String> coloredLore = lore.stream()
                .map(TextUtils::applyColor)
                .collect(Collectors.toList());

        this.meta.setLore(coloredLore);
        return this;
    }

    /**
     * Set the custom model data of the item
     *
     * @param customModelData The custom model data of the item
     * @return The ItemBuilder instance
     */
    public ItemBuilder setCustomModelData(final int customModelData) {
        this.meta.setCustomModelData(customModelData);
        return this;
    }

    /**
     * Set the head texture of the item
     *
     * @param texture The head texture of the item
     * @return The ItemBuilder instance
     */
    public ItemBuilder setHeadTexture(final String texture) {
        if (texture != null && this.meta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) this.meta;
            SkullMeta headMeta = (SkullMeta) HeadUtil.itemFromUrl("https://textures.minecraft.net/texture/" + texture).getItemMeta();

            if (headMeta != null) {
                skullMeta.setOwnerProfile(headMeta.getOwnerProfile());
            }
        }
        return this;
    }

    /**
     * Build the item
     *
     * @return The built item
     */
    public ItemStack build() {
        this.itemStack.setItemMeta(this.meta);
        return this.itemStack;
    }

    /**
     * Get the type of the item
     *
     * @return The type of the item
     */
    public Material getType() {
        return this.itemStack.getType();
    }
}
