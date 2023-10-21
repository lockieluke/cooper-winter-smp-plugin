package me.lockie.coopersmpwinter

import org.bukkit.NamespacedKey
import org.bukkit.entity.Damageable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*

class WinterEventListener(private val plugin: CooperSMPWinter) : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerDamageBlock(event: BlockDamageEvent) {
        val itemInHand = event.player.inventory.itemInMainHand
        val id = itemInHand.itemMeta.persistentDataContainer.get(
            requireNotNull(NamespacedKey.fromString("id")),
            PersistentDataType.STRING
        )
        if (!Objects.isNull(id) && id == "snow_shovel" && !event.block.type.toString()
                .contains("SNOW")
        ) event.isCancelled = true
    }

    //    @EventHandler
    //    public void onBlockPlaced(BlockPlaceEvent event) {
    //        Block placedBlock = event.getBlockPlaced();
    //        Material placedBlockType = placedBlock.getType();
    //
    //        if (placedBlockType.equals(Material.PLAYER_HEAD)) {
    //            ItemStack item = (ItemStack) placedBlock.getDrops().toArray()[0];
    //            SkullMeta meta = (SkullMeta) item.getItemMeta();
    //            ProfileProperty profileProperty = Objects.requireNonNull(meta.getPlayerProfile()).getProperties().stream().filter(p -> p.getName().equals("textures")).findFirst().orElse(null);
    //            String texture = Objects.requireNonNull(profileProperty).getValue();
    //            Location blockLocation = placedBlock.getLocation();
    //
    //            if (texture.equals(SkullTexture.HotChocolate)) {
    //                BukkitTask task = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
    //                    Bukkit.getLogger().info("running");
    //                    World world = placedBlock.getWorld();
    //                    if (world.getBlockAt(blockLocation) == placedBlock)
    //                        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, blockLocation.add(0, 0.5, 0), 20, 0.1);
    //                }, 0, 100);
    //            }
    //        }
    //    }
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onSnowballHit(event: ProjectileHitEvent) {
        val hitEntity = event.hitEntity
        if (!Objects.isNull(hitEntity) && hitEntity is Damageable) {
            hitEntity.damage(plugin.getConfig().getDouble("snowball-damage"))
        }
    }
}
