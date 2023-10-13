package me.lockie.coopersmpwinter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.Objects;

public class CustomItemManager {

    public static ItemStack snowShovel;

    private final CooperSMPWinter plugin;
    public CustomItemManager(CooperSMPWinter plugin) {
        this.plugin = plugin;
    }

    private void registerSnowballShovel() {
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

        ShapedRecipe shapedRecipe = new ShapedRecipe(NamespacedKey.minecraft("snow_shovel"), snowShovel);
        shapedRecipe.shape(" A ", " B ", " B ");
        shapedRecipe.setIngredient('A', Material.STONE);
        shapedRecipe.setIngredient('B', Material.STICK);

        if (!Bukkit.getRecipesFor(snowShovel).isEmpty())
            Bukkit.removeRecipe(shapedRecipe.getKey());

        Bukkit.addRecipe(shapedRecipe);

        CustomItemManager.snowShovel = snowShovel;
    }

    public void registerCustomItems() {
        this.registerSnowballShovel();
    }

}
