package dev.eychro.solarHub.Listeners.World;

import dev.eychro.solarHub.Commands.Admin.BuildMode;
import dev.eychro.solarHub.SolarHub;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class ItemProtection implements Listener {

    private final SolarHub plugin;
    private final BuildMode buildMode;

    public ItemProtection(SolarHub plugin, BuildMode buildMode) {
        this.plugin = plugin;
        this.buildMode = buildMode;
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }
        if (buildMode.hasBuildMode(player.getUniqueId())) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (buildMode.hasBuildMode(e.getPlayer().getUniqueId())) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent e) {
        if (buildMode.hasBuildMode(e.getPlayer().getUniqueId())) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof dev.eychro.solarHub.Managers.MenuHolder) {
            return;
        }
        if (buildMode.hasBuildMode(e.getWhoClicked().getUniqueId())) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof dev.eychro.solarHub.Managers.MenuHolder) {
            return;
        }
        if (buildMode.hasBuildMode(e.getWhoClicked().getUniqueId())) {
            return;
        }
        e.setCancelled(true);
    }
}