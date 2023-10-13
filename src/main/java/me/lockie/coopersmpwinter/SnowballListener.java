package me.lockie.coopersmpwinter;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.Objects;

public class SnowballListener implements Listener {

    private final CooperSMPWinter plugin;
    public SnowballListener(CooperSMPWinter plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSnowballHit(ProjectileHitEvent event) {
        Entity hitEntity = event.getHitEntity();
        if (!Objects.isNull(hitEntity) && hitEntity instanceof Damageable) {
            ((Damageable) hitEntity).damage(this.plugin.getConfig().getDouble("snowball-damage"));
        }
    }

}
