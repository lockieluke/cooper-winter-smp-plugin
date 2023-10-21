package me.lockie.coopersmpwinter.items;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.Objects;

public class SnowShovel extends CustomItem {

    public SnowShovel() {
        super("snow_shovel");
    }

    @Override
    public void registerItem() {
        ItemStack snowShovel = new ItemStack(Material.STONE_SHOVEL);
        snowShovel.setAmount(1);
        snowShovel.editMeta(meta -> {
            meta.getPersistentDataContainer().set(Objects.requireNonNull(NamespacedKey.fromString("id")), PersistentDataType.STRING, "snow_shovel");
            meta.displayName(Component.text(String.format("%sSnow Shovel", ChatColor.AQUA)));
            meta.setUnbreakable(true);
            meta.lore(Collections.singletonList(Component.text(String.format("%sA shovel that can be used to collect snowballs", ChatColor.WHITE))));
        });
        snowShovel.addUnsafeEnchantment(Enchantment.DIG_SPEED, 99);
        snowShovel.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        snowShovel.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        this.item = snowShovel;
    }

    @Override
    public void registerRecipe() {
        ShapedRecipe shapedRecipe = new ShapedRecipe(NamespacedKey.minecraft("snow_shovel"), this.item);
        shapedRecipe.shape(" A ", " B ", " B ");
        shapedRecipe.setIngredient('A', Material.STONE);
        shapedRecipe.setIngredient('B', Material.STICK);

        if (!Bukkit.getRecipesFor(this.item).isEmpty())
            Bukkit.removeRecipe(shapedRecipe.getKey());

        Bukkit.addRecipe(shapedRecipe);

    }
}
