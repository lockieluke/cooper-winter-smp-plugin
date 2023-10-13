package me.lockie.coopersmpwinter;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class WinterEventListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamageBlock(BlockDamageEvent event) {
        final ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
        final String id = Objects.requireNonNull(itemInHand.getItemMeta().getPersistentDataContainer().get(Objects.requireNonNull(NamespacedKey.fromString("id")), PersistentDataType.STRING));

        if (id.equals("snow_shovel") && !event.getBlock().getType().toString().contains("SNOW"))
            event.setCancelled(true);
    }

}
