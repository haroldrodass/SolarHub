package dev.eychro.solarHub.Listeners.Player;

import dev.eychro.solarHub.SolarHub;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class PlayerProtection implements Listener {

    private final SolarHub plugin;

    public PlayerProtection(SolarHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }

        e.setCancelled(true);

        if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
            Location spawn = JoinListener.getSpawn(plugin);
            if (spawn == null) {
                spawn = player.getWorld().getSpawnLocation();
            }
            player.setFallDistance(0);
            player.teleport(spawn);
        }
    }

    @EventHandler
    public void onPVP(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            e.setCancelled(true);
            e.setFoodLevel(20);
        }
    }
}