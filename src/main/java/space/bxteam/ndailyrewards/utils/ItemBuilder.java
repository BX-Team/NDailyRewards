package space.bxteam.ndailyrewards.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class ItemBuilder {
    private final ItemStack itemStack;
    private final ItemMeta meta;

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.meta = itemStack.getItemMeta();
    }

    public ItemBuilder setName(final String name) {
        this.meta.setDisplayName(TextUtils.applyColor(name));
        return this;
    }

    public ItemBuilder setLore(final List<String> lore) {
        List<String> coloredLore = lore.stream()
                .map(TextUtils::applyColor)
                .collect(Collectors.toList());

        this.meta.setLore(coloredLore);
        return this;
    }

    public ItemBuilder setCustomModelData(final int customModelData) {
        this.meta.setCustomModelData(customModelData);
        return this;
    }

    public ItemStack build() {
        this.itemStack.setItemMeta(this.meta);
        return this.itemStack;
    }

    public Material getType() {
        return this.itemStack.getType();
    }
}
