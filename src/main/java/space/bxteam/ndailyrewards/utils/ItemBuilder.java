package space.bxteam.ndailyrewards.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ItemBuilder {
    private final ItemStack itemStack;
    private final ItemMeta meta;

    private static final Pattern customModelPattern = Pattern.compile("CustomModel\\[(\\w+):(\\d+)]\\{(\\d+)}");
    private static final Pattern customSkullPattern = Pattern.compile("CustomSkull\\[(\\w+):(\\d+)]\\{(UUID|URL|BASE64):(\\S+)}");
    private static final Pattern defaultPattern = Pattern.compile("(\\w+):(\\d+)");

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.meta = itemStack.getItemMeta();
    }

    /**
     * Parse an item stack from a custom string format
     *
     * @param input The input string (e.g. "DIAMOND:1", "CustomModel[DIAMOND:1]{1}", "CustomSkull[PLAYER_HEAD:1]{UUID:1234-5678-9012-3456}")
     * @return The parsed item stack
     */
    public static ItemStack parseItemStack(String input) {
        Matcher customModelMatcher = customModelPattern.matcher(input);
        Matcher customSkullMatcher = customSkullPattern.matcher(input);
        Matcher defaultMatcher = defaultPattern.matcher(input);

        if (customModelMatcher.matches()) {
            Material material = Material.valueOf(customModelMatcher.group(1));
            int quantity = Integer.parseInt(customModelMatcher.group(2));
            int customModelData = Integer.parseInt(customModelMatcher.group(3));
            return new ItemBuilder(new ItemStack(material, quantity))
                    .setCustomModelData(customModelData)
                    .build();
        } else if (customSkullMatcher.matches()) {
            Material material = Material.valueOf(customSkullMatcher.group(1));
            int quantity = Integer.parseInt(customSkullMatcher.group(2));
            String type = customSkullMatcher.group(3);
            String value = customSkullMatcher.group(4);

            ItemStack skullItem = new ItemStack(material, quantity);
            if (material == Material.PLAYER_HEAD) {
                skullItem = switch (type) {
                    case "UUID" -> HeadUtil.itemFromUuid(UUID.fromString(value));
                    case "URL" -> HeadUtil.itemFromUrl(value);
                    case "BASE64" -> HeadUtil.itemFromBase64(value);
                    default -> skullItem;
                };
                skullItem.setAmount(quantity);
            }

            return skullItem;
        } else if (defaultMatcher.matches()) {
            Material material = Material.valueOf(defaultMatcher.group(1));
            int quantity = Integer.parseInt(defaultMatcher.group(2));
            return new ItemStack(material, quantity);
        }

        throw new IllegalArgumentException("Invalid item string: " + input);
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
