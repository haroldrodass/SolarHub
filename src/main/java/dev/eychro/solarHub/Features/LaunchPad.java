package dev.eychro.solarHub.Features;

import dev.eychro.solarHub.SolarHub;
import dev.eychro.solarHub.Utils.RegistryUtil;
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

        String soundName = config.getString("LaunchPad.Sound", "ENTITY_FIREWORK_ROCKET_LAUNCH");
        Sound sound = RegistryUtil.getSound(soundName);

        if (sound == null && soundName != null && !soundName.isEmpty() && !soundName.equalsIgnoreCase("NONE")) {
            plugin.getLogger().warning("LaunchPad.Sound '" + soundName + "' no es válido. Usa 'NONE' para desactivarlo.");
        }

        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }

        if (!config.getBoolean("LaunchPad.particle.enabled", false)) {
            return;
        }

        String particleName = config.getString("LaunchPad.particle.type", "FLAME");
        final Particle particle = RegistryUtil.getParticle(particleName);

        if (particle == null) {
            plugin.getLogger().warning("LaunchPad.particle.type '" + particleName + "' no es una partícula válida.");
            return;
        }

        final boolean visibleForEveryone = config.getBoolean("LaunchPad.particle.Visible-For-Everyone", true);

        new BukkitRunnable() {
            int tiempoEnAire = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    cancel();
                    return;
                }

                if (visibleForEveryone) {
                    player.getWorld().spawnParticle(particle, player.getLocation().add(0, 0.5, 0), 3, 0.2, 0.2, 0.2, 0.0);
                } else {
                    player.spawnParticle(particle, player.getLocation().add(0, 0.5, 0), 3, 0.2, 0.2, 0.2, 0.0);
                }

                tiempoEnAire++;

                if (tiempoEnAire > 60
                        || (tiempoEnAire > 5 && player.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid())) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}