package dev.eychro.solarHub.Listeners.Menu;

import dev.eychro.solarHub.Managers.MenuHolder;
import dev.eychro.solarHub.Managers.MenuManager;
import dev.eychro.solarHub.SolarHub;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class MenuListener implements Listener {

    private final SolarHub plugin;

    public MenuListener(SolarHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMenuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        if (e.getInventory().getHolder() instanceof MenuHolder) {
            e.setCancelled(true);

            // Solo procesar clics dentro del menú superior (no en el inventario propio del jugador)
            if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView().getTopInventory()) {
                return;
            }

            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            ItemMeta meta = clicked.getItemMeta();
            if (meta == null) return;

            NamespacedKey actionKey = new NamespacedKey(plugin, MenuManager.ACTION_PDC_KEY);
            if (!meta.getPersistentDataContainer().has(actionKey, PersistentDataType.STRING)) {
                return;
            }

            String action = meta.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
            if (action == null || action.isEmpty()) return;

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            plugin.getMenuManager().executeAction(player, action);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMenuDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof MenuHolder) {
            e.setCancelled(true);
        }
    }
}
