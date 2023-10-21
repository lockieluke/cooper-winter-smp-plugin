package me.lockie.coopersmpwinter.items

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

class SnowShovel : CustomItem("snow_shovel") {
    override fun registerItem() {
        val snowShovel = ItemStack(Material.STONE_SHOVEL)
        snowShovel.amount = 1
        snowShovel.editMeta { meta: ItemMeta ->
            meta.persistentDataContainer.set(
                requireNotNull(NamespacedKey.fromString("id")),
                PersistentDataType.STRING,
                "snow_shovel"
            )
            meta.displayName(Component.text(String.format("%sSnow Shovel", ChatColor.AQUA)))
            meta.isUnbreakable = true
            meta.lore(
                listOf(
                    Component.text(
                        String.format(
                            "%sA shovel that can be used to collect snowballs",
                            ChatColor.WHITE
                        )
                    )
                )
            )
        }
        snowShovel.addUnsafeEnchantment(Enchantment.DIG_SPEED, 99)
        snowShovel.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        snowShovel.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        item = snowShovel
    }

    override fun registerRecipe() {
        val shapedRecipe = ShapedRecipe(NamespacedKey.minecraft("snow_shovel"), item!!)
        shapedRecipe.shape(" A ", " B ", " B ")
        shapedRecipe.setIngredient('A', Material.STONE)
        shapedRecipe.setIngredient('B', Material.STICK)
        if (!Bukkit.getRecipesFor(item!!).isEmpty()) Bukkit.removeRecipe(shapedRecipe.key)
        Bukkit.addRecipe(shapedRecipe)
    }
}
