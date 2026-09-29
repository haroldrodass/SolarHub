package dev.eychro.solarHub.Features;

import dev.eychro.solarHub.SolarHub;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

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

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (!enabled() || !player.hasPermission("Solar.DoubleJump")) {
            return;
        }
        if (isValidMode(player)) {
            player.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onDoubleJump(PlayerToggleFlightEvent e) {
        if (!enabled()) {
            return;
        }

        Player p = e.getPlayer();

        if (!p.hasPermission("Solar.DoubleJump") || !isValidMode(p)) {
            return;
        }

        e.setCancelled(true);
        p.setAllowFlight(false);
        p.setFlying(false);

        double power = plugin.getFiles().getConfig().getDouble("DoubleJump.power", 0.5);
        double height = plugin.getFiles().getConfig().getDouble("DoubleJump.height", 1.0);

        p.setVelocity(p.getLocation().getDirection().multiply(power).setY(height));
        p.playSound(p.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        if (p.getAllowFlight() || !enabled() || !isValidMode(p) || !p.hasPermission("Solar.DoubleJump")) {
            return;
        }

        if (p.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid()) {
            p.setAllowFlight(true);
        }
    }
}