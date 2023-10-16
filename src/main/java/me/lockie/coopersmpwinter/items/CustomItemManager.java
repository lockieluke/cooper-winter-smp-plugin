package me.lockie.coopersmpwinter.items;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class CustomItemManager {

    public ArrayList<CustomItem> customItems = new ArrayList<>();

    public ItemStack getCustomItemById(String id) {
        for (CustomItem customItem : this.customItems)
            if (customItem.id.equals(id))
                return customItem.item;
        return null;
    }

    public CustomItemManager() {
        customItems.add(new SnowShovel());
        customItems.add(new HotChocolate());
    }

    public void registerCustomItems() {
        this.customItems.forEach(customItem -> {
            customItem.registerItem();
            customItem.registerRecipe();
        });
    }

}
