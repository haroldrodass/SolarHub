package dev.eychro.solarHub.Listeners.Player;

import dev.eychro.solarHub.SolarHub;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinMessage implements Listener {

    private final SolarHub plugin;

    public JoinMessage(SolarHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (player.hasPermission("Solar.JoinMessage")) {
            String mensaje = plugin.getFiles().getMessage("JoinMessage");
            if (mensaje != null) {
                e.setJoinMessage(mensaje.replace("%player%", player.getName()));
                return;
            }
        }

        e.setJoinMessage(null);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
    }
}