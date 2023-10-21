package me.lockie.coopersmpwinter.items

import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

class CustomItemManager {
    var customItems = ArrayList<CustomItem>()
    fun getCustomItemById(id: String): ItemStack? {
        for (customItem in customItems) if (customItem.id == id) return customItem.item
        return null
    }

    init {
        customItems.add(SnowShovel())
        customItems.add(HotChocolate())
    }

    fun registerCustomItems() {
        customItems.forEach(Consumer { customItem: CustomItem ->
            customItem.registerItem()
            customItem.registerRecipe()
        })
    }
}
