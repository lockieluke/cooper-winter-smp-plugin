package me.lockie.coopersmpwinter;

import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

public class WinterEventListener implements Listener {

    private final CooperSMPWinter plugin;

    public WinterEventListener(CooperSMPWinter plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamageBlock(BlockDamageEvent event) {
        final ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
        final String id = Objects.requireNonNull(itemInHand.getItemMeta().getPersistentDataContainer().get(Objects.requireNonNull(NamespacedKey.fromString("id")), PersistentDataType.STRING));

        if (id.equals("snow_shovel") && !event.getBlock().getType().toString().contains("SNOW"))
            event.setCancelled(true);
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
    public void onSnowballHit(ProjectileHitEvent event) {
        Entity hitEntity = event.getHitEntity();
        if (!Objects.isNull(hitEntity) && hitEntity instanceof Damageable) {
            ((Damageable) hitEntity).damage(this.plugin.getConfig().getDouble("snowball-damage"));
        }
    }

}
