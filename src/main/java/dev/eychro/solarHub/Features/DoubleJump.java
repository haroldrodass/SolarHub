package dev.eychro.solarHub.Features;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class DoubleJump implements Listener {

    private final dev.eychro.solarHub.SolarHub plugin;

    public DoubleJump(dev.eychro.solarHub.SolarHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (!plugin.getConfig().getBoolean("DoubleJump.enabled")) {
            return;
        }

        if (!e.getPlayer().hasPermission("Solar.DoubleJump")) {
            e.getPlayer().sendMessage(plugin.getFiles().getConfig().getString("No-permission"));
            return;
        }

        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            player.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onDoubleJump(PlayerToggleFlightEvent e) {
        if (!plugin.getConfig().getBoolean("DoubleJump.enabled")) {
            return;
        }

        Player p = e.getPlayer();

        if (!p.hasPermission("Solar.DoubleJump")) {
            return;
        }

        if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
            e.setCancelled(true);
            p.setAllowFlight(false);
            p.setFlying(false);

            double power = plugin.getConfig().getDouble("DoubleJump.power", 0.5);
            double height = plugin.getConfig().getDouble("DoubleJump.height", 1.0);

            p.setVelocity(p.getLocation().getDirection().multiply(power).setY(height));
            p.playSound(p.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        if (!plugin.getConfig().getBoolean("DoubleJump.enabled")) {
            return;
        }

        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        if (!p.hasPermission("Solar.DoubleJump")) {
            return;
        }

        if (!p.getAllowFlight() && p.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid()) {
            p.setAllowFlight(true);
        }
    }
}