package dev.eychro.solarHub.Features;

import dev.eychro.solarHub.SolarHub;
import dev.eychro.solarHub.Utils.RegistryUtil;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class DoubleJump implements Listener {

    private final SolarHub plugin;

    public DoubleJump(SolarHub plugin) {
        this.plugin = plugin;
    }

    private boolean enabled() {
        return plugin.getFiles().getConfig().getBoolean("DoubleJump.enabled", true);
    }

    private boolean isValidMode(Player p) {
        return p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE;
    }

    private boolean hasFly(Player p) {
        return plugin.getFlyManager().has(p.getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (hasFly(player)) {
            return;
        }
        if (!enabled() || !player.hasPermission("Solar.DoubleJump")) {
            return;
        }
        if (isValidMode(player)) {
            player.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getFlyManager().remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDoubleJump(PlayerToggleFlightEvent e) {
        if (!enabled()) {
            return;
        }

        Player p = e.getPlayer();

        if (hasFly(p)) {
            return;
        }

        if (!p.hasPermission("Solar.DoubleJump") || !isValidMode(p)) {
            return;
        }

        e.setCancelled(true);
        p.setAllowFlight(false);
        p.setFlying(false);

        FileConfiguration config = plugin.getFiles().getConfig();
        double power = config.getDouble("DoubleJump.power", 0.5);
        double height = config.getDouble("DoubleJump.height", 1.0);

        p.setVelocity(p.getLocation().getDirection().multiply(power).setY(height));

        String soundName = config.getString("DoubleJump.Sound", "ENTITY_BAT_TAKEOFF");
        Sound sound = RegistryUtil.getSound(soundName);

        if (sound == null && soundName != null && !soundName.isEmpty() && !soundName.equalsIgnoreCase("NONE")) {
            plugin.getLogger().warning("DoubleJump.Sound '" + soundName + "' no es válido. Usa 'NONE' para desactivarlo.");
        }

        if (sound != null) {
            p.playSound(p.getLocation(), sound, 1.0f, 1.0f);
        }

        if (!config.getBoolean("DoubleJump.particle.enabled", false)) {
            return;
        }

        String particleName = config.getString("DoubleJump.particle.type", "CLOUD");
        Particle particle = RegistryUtil.getParticle(particleName);

        if (particle == null) {
            plugin.getLogger().warning("DoubleJump.particle.type '" + particleName + "' no es una partícula válida.");
            return;
        }

        final boolean visibleForEveryone = config.getBoolean("DoubleJump.particle.Visible-For-Everyone", true);

        new BukkitRunnable() {
            int tiempoEnAire = 0;

            @Override
            public void run() {
                if (!p.isOnline() || p.isDead()) {
                    cancel();
                    return;
                }

                if (visibleForEveryone) {
                    p.getWorld().spawnParticle(particle, p.getLocation().add(0, 0.5, 0), 3, 0.2, 0.2, 0.2, 0.0);
                } else {
                    p.spawnParticle(particle, p.getLocation().add(0, 0.5, 0), 3, 0.2, 0.2, 0.2, 0.0);
                }

                tiempoEnAire++;

                if (tiempoEnAire > 60 || (tiempoEnAire > 5 && p.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid())) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        if (hasFly(p)) {
            return;
        }

        if (p.getAllowFlight() || !enabled() || !isValidMode(p) || !p.hasPermission("Solar.DoubleJump")) {
            return;
        }

        if (p.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid()) {
            p.setAllowFlight(true);
        }
    }
}