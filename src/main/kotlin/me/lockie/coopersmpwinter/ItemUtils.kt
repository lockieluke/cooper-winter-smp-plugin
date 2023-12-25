package me.lockie.coopersmpwinter

import org.bukkit.inventory.ItemStack

fun ItemStack.isDisc(): Boolean {
    return this.type.name.endsWith("_DISC")
}