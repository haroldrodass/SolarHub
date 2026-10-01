package dev.eychro.solarHub.Managers;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.eychro.solarHub.SolarHub;
import dev.eychro.solarHub.Utils.RegistryUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class MenuManager {

    private final SolarHub plugin;
    public static final String ACTION_PDC_KEY = "solar_menu_action";

    public MenuManager(SolarHub plugin) {
        this.plugin = plugin;
    }

    public boolean menuExists(String menuId) {
        return plugin.getFiles().getConfig().isConfigurationSection("Menus." + menuId.toLowerCase());
    }

    public void openMenu(Player player, String menuId) {
        String lowerId = menuId.toLowerCase();
        FileConfiguration config = plugin.getFiles().getConfig();

        if (!config.isConfigurationSection("Menus." + lowerId)) {
            player.sendMessage(plugin.getFiles().getMessage("MenuNotFound", "%menu%", menuId));
            return;
        }

        ConfigurationSection section = config.getConfigurationSection("Menus." + lowerId);
        if (section == null) return;

        String rawTitle = section.getString("title", "&8Menú");
        String formattedTitle = FileManager.color(rawTitle.replace("%player%", player.getName()));
        int rows = Math.max(1, Math.min(6, section.getInt("rows", 3)));
        int size = rows * 9;

        Inventory inv = Bukkit.createInventory(
                new MenuHolder(lowerId),
                size,
                LegacyComponentSerializer.legacySection().deserialize(formattedTitle)
        );

        // Decoración de fondo (Filler)
        if (section.getBoolean("filler.enabled", false)) {
            String fillerMatName = section.getString("filler.material", "GRAY_STAINED_GLASS_PANE");
            Material fillerMat = Material.matchMaterial(fillerMatName);
            if (fillerMat == null) fillerMat = Material.GRAY_STAINED_GLASS_PANE;

            String fillerName = FileManager.color(section.getString("filler.name", " "));
            ItemStack fillerItem = new ItemStack(fillerMat);
            ItemMeta fillerMeta = fillerItem.getItemMeta();
            if (fillerMeta != null) {
                fillerMeta.setDisplayName(fillerName);
                fillerItem.setItemMeta(fillerMeta);
            }

            for (int i = 0; i < size; i++) {
                inv.setItem(i, fillerItem);
            }
        }

        // Ítems configurados del menú
        ConfigurationSection itemsSection = section.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSec = itemsSection.getConfigurationSection(key);
                if (itemSec == null) continue;

                int slot = itemSec.getInt("slot", 0);
                if (slot < 0 || slot >= size) continue;

                ItemStack item = buildMenuItem(player, itemSec);
                if (item != null) {
                    inv.setItem(slot, item);
                }
            }
        }

        // Sonido al abrir
        String soundName = section.getString("sound", "NONE");
        Sound sound = RegistryUtil.getSound(soundName);
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 0.7f, 1.0f);
        }

        player.openInventory(inv);
    }

    private ItemStack buildMenuItem(Player player, ConfigurationSection sec) {
        String matName = sec.getString("material", "STONE");
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.STONE;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // Soporte para cabeza del jugador
        if (mat == Material.PLAYER_HEAD && meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
        }

        // Nombre
        String rawName = sec.getString("name", "");
        if (!rawName.isEmpty()) {
            meta.setDisplayName(FileManager.color(replacePlaceholders(rawName, player)));
        }

        // Lore
        List<String> rawLore = sec.getStringList("lore");
        if (!rawLore.isEmpty()) {
            List<String> lore = new ArrayList<>();
            for (String line : rawLore) {
                lore.add(FileManager.color(replacePlaceholders(line, player)));
            }
            meta.setLore(lore);
        }

        // Glow (brillo encantado)
        if (sec.getBoolean("glow", false)) {
            try {
                meta.setEnchantmentGlintOverride(true);
            } catch (Throwable t) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        }

        // Acción almacenada en PDC
        String action = sec.getString("action", "");
        if (!action.isEmpty()) {
            NamespacedKey key = new NamespacedKey(plugin, ACTION_PDC_KEY);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, action);
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public String replacePlaceholders(String text, Player player) {
        if (text == null) return "";
        return text
                .replace("%player%", player.getName())
                .replace("%ping%", String.valueOf(player.getPing()))
                .replace("%world%", player.getWorld().getName())
                .replace("%fly_status%", plugin.getFlyManager().has(player.getUniqueId()) ? "&aActivado" : "&cDesactivado")
                .replace("%vanish_status%", plugin.getVanish().isVanished(player.getUniqueId()) ? "&aActivado" : "&cDesactivado")
                .replace("%visibility_status%", plugin.getVisibilityManager().isHidingPlayers(player.getUniqueId()) ? "&cOcultos" : "&aVisibles");
    }

    public void executeAction(Player player, String rawAction) {
        if (rawAction == null || rawAction.isBlank()) return;

        String action = rawAction.trim();

        if (action.equalsIgnoreCase("CLOSE")) {
            player.closeInventory();
            return;
        }

        if (action.equalsIgnoreCase("VISIBILITY")) {
            plugin.getVisibilityManager().toggleVisibility(player);
            // Si tiene el menú de perfil abierto, refrescar para actualizar estado
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder holder && holder.getMenuId().equals("profile")) {
                openMenu(player, "profile");
            }
            return;
        }

        if (action.startsWith("MENU:")) {
            String targetMenu = action.substring(5).trim();
            openMenu(player, targetMenu);
            return;
        }

        if (action.startsWith("COMMAND:")) {
            String cmd = action.substring(8).trim().replace("%player%", player.getName());
            player.closeInventory();
            player.performCommand(cmd);
            return;
        }

        if (action.startsWith("CONSOLE:")) {
            String cmd = action.substring(8).trim().replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            return;
        }

        if (action.startsWith("MESSAGE:")) {
            String msg = action.substring(8).trim().replace("%player%", player.getName());
            player.sendMessage(FileManager.color(msg));
            return;
        }

        if (action.startsWith("CONNECT:") || action.startsWith("SERVER:")) {
            String server = action.contains(":") ? action.substring(action.indexOf(':') + 1).trim() : "";
            connectToServer(player, server);
            return;
        }

        if (action.startsWith("COSMETIC_PARTICLE:")) {
            String particleId = action.substring(18).trim();
            plugin.getCosmeticsManager().setParticle(player, particleId);
            openMenu(player, "cosmetics");
            return;
        }

        if (action.startsWith("COSMETIC_HAT:")) {
            String matName = action.substring(13).trim();
            Material mat = Material.matchMaterial(matName);
            if (mat != null) {
                plugin.getCosmeticsManager().setHat(player, mat);
                openMenu(player, "cosmetics");
            }
            return;
        }

        if (action.equalsIgnoreCase("COSMETIC_SPEED")) {
            plugin.getCosmeticsManager().toggleSpeed(player);
            openMenu(player, "cosmetics");
            return;
        }

        if (action.equalsIgnoreCase("COSMETIC_JUMP")) {
            plugin.getCosmeticsManager().toggleJump(player);
            openMenu(player, "cosmetics");
            return;
        }

        if (action.equalsIgnoreCase("COSMETIC_CLEAR")) {
            plugin.getCosmeticsManager().clearAll(player);
            openMenu(player, "cosmetics");
        }
    }

    public void connectToServer(Player player, String serverName) {
        if (serverName == null || serverName.isBlank()) return;

        player.sendMessage(plugin.getFiles().getMessage("ConnectingServer", "%server%", serverName));
        player.closeInventory();

        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().warning("Error enviando jugador a BungeeCord server " + serverName + ": " + e.getMessage());
        }
    }
}
