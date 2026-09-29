package dev.eychro.solarHub.Listeners.World;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class MobsSpawn implements Listener {

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }
}
