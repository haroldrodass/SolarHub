package dev.eychro.solarHub.Managers;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class MenuHolder implements InventoryHolder {

    private final String menuId;

    public MenuHolder(String menuId) {
        this.menuId = menuId;
    }

    public String getMenuId() {
        return menuId;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
