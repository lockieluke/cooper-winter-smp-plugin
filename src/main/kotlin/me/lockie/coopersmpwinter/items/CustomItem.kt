package me.lockie.coopersmpwinter.items

import org.bukkit.inventory.ItemStack

abstract class CustomItem(var id: String) {
    var item: ItemStack? = null

    abstract fun registerItem()
    abstract fun registerRecipe()
}
