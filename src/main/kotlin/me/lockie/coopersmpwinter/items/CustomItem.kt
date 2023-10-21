package me.lockie.coopersmpwinter.items;

import org.bukkit.inventory.ItemStack;

public abstract class CustomItem {
    protected String id;
    protected ItemStack item;

    public CustomItem(String id) {
        this.id = id;
    }

    public abstract void registerItem();
    public abstract void registerRecipe();

    public String getId() {
        return id;
    }

    public ItemStack getItem() {
        return item;
    }
}
