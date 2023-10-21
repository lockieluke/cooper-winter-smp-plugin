package me.lockie.coopersmpwinter.items;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.lockie.coopersmpwinter.SkullTexture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class HotChocolate extends CustomItem {

    public HotChocolate() {
        super("hot_chocolate");
    }

    @Override
    public void registerItem() {
        ItemStack hotChocolate = new ItemStack(Material.PLAYER_HEAD, 1);
        hotChocolate.editMeta(meta -> {
            meta.getPersistentDataContainer().set(Objects.requireNonNull(NamespacedKey.fromString("id")), PersistentDataType.STRING, this.id);

            SkullMeta skullMeta = (SkullMeta) meta;
            skullMeta.displayName(Component.text("Hot Chocolate").color(NamedTextColor.GOLD));
            skullMeta.lore(Collections.singletonList(Component.text("A nice warm drink to keep you warm during the winter").color(NamedTextColor.GRAY)));

            PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID(), null);
            playerProfile.setProperty(new ProfileProperty("textures", SkullTexture.HotChocolate));
            skullMeta.setPlayerProfile(playerProfile);
        });

        this.item = hotChocolate;
    }

    @Override
    public void registerRecipe() {

    }
}
