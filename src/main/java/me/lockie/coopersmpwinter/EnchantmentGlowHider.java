package me.lockie.coopersmpwinter;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class EnchantmentGlowHider extends PacketAdapter {

    public EnchantmentGlowHider(Plugin plugin) {
        super(plugin, PacketType.Play.Server.SET_SLOT);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
            final ItemStack item = event.getPacket().getItemModifier().read(0);
            final String id = Objects.requireNonNull(item.getItemMeta().getPersistentDataContainer().get(Objects.requireNonNull(NamespacedKey.fromString("id")), PersistentDataType.STRING));

            if (id.equals("snow_shovel"))
                item.removeEnchantment(item.getEnchantments().keySet().iterator().next());

            event.getPacket().getItemModifier().write(0, item);
        }
    }

}
