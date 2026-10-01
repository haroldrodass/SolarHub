package dev.eychro.solarHub.Listeners.Player;

import dev.eychro.solarHub.Managers.ParkourManager;
import dev.eychro.solarHub.SolarHub;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class ParkourListener implements Listener {

    private final SolarHub plugin;
    private final ParkourManager parkourManager;

    public ParkourListener(SolarHub plugin, ParkourManager parkourManager) {
        this.plugin = plugin;
        this.parkourManager = parkourManager;
    }

    @EventHandler
    public void onPressurePlate(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;

        if (!plugin.getFiles().getConfig().getBoolean("Parkour.enabled", true)) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();

        String startMatName = plugin.getFiles().getConfig().getString("Parkour.start-material", "LIGHT_WEIGHTED_PRESSURE_PLATE");
        String checkMatName = plugin.getFiles().getConfig().getString("Parkour.checkpoint-material", "HEAVY_WEIGHTED_PRESSURE_PLATE");

        Material startMat = Material.matchMaterial(startMatName);
        Material checkMat = Material.matchMaterial(checkMatName);

        if (startMat != null && block.getType() == startMat) {

            if (parkourManager.isExitLocked(player)) {
                parkourManager.extendExitLock(player);
                return;
            }

            Location plateCenter = block.getLocation().add(0.5, 0.1, 0.5);
            boolean configured = parkourManager.hasStartPlate();
            boolean isStart = configured
                    ? parkourManager.isStartPlate(block)
                    : isSameBlock(parkourManager.getStartLocation(player), block.getLocation());

            if (!parkourManager.isPlaying(player)) {

                if (!configured || isStart) {
                    parkourManager.startParkour(player, plateCenter);
                }
            } else if (isStart) {
                long elapsed = System.currentTimeMillis() - parkourManager.getStartTime(player);
                if (elapsed > 2000) {
                    parkourManager.startParkour(player, plateCenter);
                }
            } else {
                parkourManager.finishParkour(player);
            }
        } else if (checkMat != null && block.getType() == checkMat) {
            if (parkourManager.isPlaying(player)) {
                parkourManager.handleCheckpoint(player, block);
            }
        }
    }

    private boolean isSameBlock(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return false;
        if (loc1.getWorld() == null || !loc1.getWorld().equals(loc2.getWorld())) return false;
        return loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onExitItem(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.BARRIER || !item.hasItemMeta()) return;

        NamespacedKey key = new NamespacedKey(plugin, ParkourManager.EXIT_PDC);
        if (!item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) return;

        event.setCancelled(true);
        parkourManager.exitParkour(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        if (parkourManager.isPlaying(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().setAllowFlight(false);
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player p && parkourManager.isPlaying(p)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player p && parkourManager.isPlaying(p)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (parkourManager.isPlaying(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        if (parkourManager.isPlaying(event.getPlayer())) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        parkourManager.cancelParkour(event.getPlayer());
    }
}