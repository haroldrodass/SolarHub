package dev.eychro.solarHub.Features;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class LaunchPad implements Listener {

    private final dev.eychro.solarHub.SolarHub plugin;

    public LaunchPad(dev.eychro.solarHub.SolarHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLaunchPad(PlayerMoveEvent e) {
        if (!plugin.getFiles().getConfig().getBoolean("LaunchPad.enabled")) {
            return;
        }

        if (!e.getPlayer().hasPermission("Solar.LaunchPad")) {
            e.getPlayer().sendMessage(plugin.getFiles().getConfig().getString("No-permission"));
            return;
        }


        if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockY() == e.getTo().getBlockY() && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) {
            return;
        }

        Block bloquePies = e.getTo().getBlock();
        Block bloqueAbajo = bloquePies.getRelative(BlockFace.DOWN);

        String configBlock = plugin.getFiles().getConfig().getString("LaunchPad.block", "SLIME_BLOCK");
        Material targetMaterial = Material.getMaterial(configBlock.toUpperCase());

        if (targetMaterial != null && (bloquePies.getType() == targetMaterial || bloqueAbajo.getType() == targetMaterial)) {

            Player player = e.getPlayer();
            double power = plugin.getFiles().getConfig().getDouble("LaunchPad.power", 1.0);
            double height = plugin.getFiles().getConfig().getDouble("LaunchPad.height", 2.0);

            player.setVelocity(player.getLocation().getDirection().multiply(power).setY(height));
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);

            if (plugin.getFiles().getConfig().getBoolean("LaunchPad.particle.enabled")) {
                new BukkitRunnable() {
                    int tiempoEnAire = 0;

                    @Override
                    public void run() {
                        if (!player.isOnline() || player.isDead()) {
                            this.cancel();
                            return;
                        }

                        Particle particle = Particle.valueOf(plugin.getFiles().getConfig().getString("LaunchPad.particle.type", "CAMPFIRE_COSY_SMOKE"));

                        player.spawnParticle(particle, player.getLocation().add(0, 0.5, 0), 3, 0.2, 0.2, 0.2, 0.0);

                        tiempoEnAire++;

                        if (tiempoEnAire > 5 && player.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid()) {
                            this.cancel();
                        } else if (tiempoEnAire > 60) {
                            this.cancel();
                        }
                    }
                }.runTaskTimer(plugin, 1L, 1L);
            }
        }
    }
}