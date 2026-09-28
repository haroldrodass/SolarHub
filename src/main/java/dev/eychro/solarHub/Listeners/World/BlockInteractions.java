package dev.eychro.solarHub.Listeners.World;

import dev.eychro.solarHub.Commands.Admin.BuildMode;
import dev.eychro.solarHub.SolarHub;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockInteractions implements Listener {

    private final SolarHub plugin;
    private final BuildMode buildMode;

    public BlockInteractions(SolarHub plugin, BuildMode buildMode) {
        this.plugin = plugin;
        this.buildMode = buildMode;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (buildMode.hasBuildMode(player.getUniqueId())) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        if (buildMode.hasBuildMode(player.getUniqueId())) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onBLockInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (buildMode.hasBuildMode(player.getUniqueId())) {
            return;
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {
            e.setCancelled(true);
        }
    }
}
