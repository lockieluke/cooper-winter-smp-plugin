package me.lockie.coopersmpwinter.items

import com.destroystokyo.paper.profile.ProfileProperty
import me.lockie.coopersmpwinter.SkullTexture
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.util.*

class HotChocolate : CustomItem("hot_chocolate") {
    override fun registerItem() {
        val hotChocolate = ItemStack(Material.PLAYER_HEAD, 1)
        hotChocolate.editMeta { meta: ItemMeta ->
            meta.persistentDataContainer.set(
                requireNotNull(NamespacedKey.fromString("id")),
                PersistentDataType.STRING,
                id
            )
            val skullMeta = meta as SkullMeta
            skullMeta.displayName(Component.text("Hot Chocolate").color(NamedTextColor.GOLD))
            skullMeta.lore(
                listOf(
                    Component.text("A nice warm drink to keep you warm during the winter").color(NamedTextColor.GRAY)
                )
            )
            val playerProfile = Bukkit.createProfile(UUID.randomUUID(), null)
            playerProfile.setProperty(ProfileProperty("textures", SkullTexture.HotChocolate))
            skullMeta.playerProfile = playerProfile
        }
        item = hotChocolate
    }

    override fun registerRecipe() {}
}
