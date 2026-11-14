package dev.eychro.solarHub.Managers;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class MenuHolder implements InventoryHolder {

    private final String menuId;
    private Inventory inventory;

    public MenuHolder(String menuId) {
        this.menuId = menuId;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (inventory == null) {
            throw new IllegalStateException("Menu inventory has not been initialized");
        }
        return inventory;
    }
}
