package dev.eychro.solarHub.Features;

import dev.eychro.solarHub.SolarHub;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class LaunchPad implements Listener {

    private final SolarHub plugin;

    public LaunchPad(SolarHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLaunchPad(PlayerMoveEvent e) {
        if (e.getTo() == null) {
            return;
        }

        if (e.getFrom().getBlockX() == e.getTo().getBlockX()
                && e.getFrom().getBlockY() == e.getTo().getBlockY()
                && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) {
            return;
        }

        FileConfiguration config = plugin.getFiles().getConfig();

        if (!config.getBoolean("LaunchPad.enabled", true)) {
            return;
        }

        Player player = e.getPlayer();

        if (!player.hasPermission("Solar.LaunchPad")) {
            return;
        }

        Block bloquePies = e.getTo().getBlock();
        Block bloqueAbajo = bloquePies.getRelative(BlockFace.DOWN);

        Material targetMaterial = Material.matchMaterial(config.getString("LaunchPad.block", "SLIME_BLOCK"));

        if (targetMaterial == null
                || (bloquePies.getType() != targetMaterial && bloqueAbajo.getType() != targetMaterial)) {
            return;
        }

        double power = config.getDouble("LaunchPad.power", 1.0);
        double height = config.getDouble("LaunchPad.height", 1.0);

        player.setVelocity(player.getLocation().getDirection().multiply(power).setY(height));
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);

        if (!config.getBoolean("LaunchPad.particle.enabled", false)) {
            return;
        }

        final Particle particle;
        try {
            particle = Particle.valueOf(config.getString("LaunchPad.particle.type", "CAMPFIRE_COSY_SMOKE").toUpperCase());
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("LaunchPad.particle.type no es una partícula válida.");
            return;
        }

        new BukkitRunnable() {
            int tiempoEnAire = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    cancel();
                    return;
                }

                player.spawnParticle(particle, player.getLocation().add(0, 0.5, 0), 3, 0.2, 0.2, 0.2, 0.0);

                tiempoEnAire++;

                if (tiempoEnAire > 60
                        || (tiempoEnAire > 5 && player.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid())) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}