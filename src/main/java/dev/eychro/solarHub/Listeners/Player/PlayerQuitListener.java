package dev.eychro.solarHub.Listeners.Player;

import dev.eychro.solarHub.Commands.Admin.BuildMode;
import dev.eychro.solarHub.SolarHub;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final SolarHub plugin;
    private final BuildMode buildMode;

    public PlayerQuitListener(SolarHub plugin, BuildMode buildMode) {
        this.plugin = plugin;
        this.buildMode = buildMode;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        if (buildMode.hasBuildMode(player.getUniqueId())) {
            buildMode.removePlayerOnQuit(player);
        }
    }
}
