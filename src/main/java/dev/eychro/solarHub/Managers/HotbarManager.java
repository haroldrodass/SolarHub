package dev.eychro.solarHub.Managers;

import dev.eychro.solarHub.SolarHub;
import dev.eychro.solarHub.Utils.RegistryUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class HotbarManager implements Listener {

    private final SolarHub plugin;
    public static final String HOTBAR_PDC_ACTION = "solar_hotbar_action";
    public static final String HOTBAR_PDC_ID = "solar_hotbar_id";

    public HotbarManager(SolarHub plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getFiles().getConfig().getBoolean("Hotbar.enabled", true);
    }

    public void giveItems(Player player) {
        if (!isEnabled()) return;
        if (!player.hasPermission("Solar.Hotbar")) return;

        // No tocar el inventario mientras está en el parkour
        if (plugin.getParkourManager() != null && plugin.getParkourManager().isPlaying(player)) return;

        // No reemplazar inventario si está en modo construcción
        if (plugin.getBuildMode() != null && plugin.getBuildMode().hasBuildMode(player.getUniqueId())) {
            return;
        }

        FileConfiguration config = plugin.getFiles().getConfig();

        if (config.getBoolean("Hotbar.clear-inventory", true)) {
            player.getInventory().clear();
        }

        ConfigurationSection itemsSec = config.getConfigurationSection("Hotbar.items");
        if (itemsSec == null) return;

        for (String id : itemsSec.getKeys(false)) {
            ConfigurationSection sec = itemsSec.getConfigurationSection(id);
            if (sec == null) continue;

            int slot = sec.getInt("slot", -1);
            if (slot < 0 || slot > 8) continue;

            ItemStack item = buildHotbarItem(player, id, sec);
            if (item != null) {
                player.getInventory().setItem(slot, item);
            }
        }

        // Sonido opcional
        String soundName = config.getString("Hotbar.sound", "NONE");
        Sound sound = RegistryUtil.getSound(soundName);
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 0.6f, 1.2f);
        }
    }

    private ItemStack buildHotbarItem(Player player, String id, ConfigurationSection sec) {
        String action = sec.getString("action", "");

        // Si es el ítem especial de visibilidad, usar configuración condicional
        if (action.equalsIgnoreCase("VISIBILITY")) {
            boolean isHiding = plugin.getVisibilityManager().isHidingPlayers(player.getUniqueId());
            ConfigurationSection stateSec = isHiding ? sec.getConfigurationSection("hidden") : sec.getConfigurationSection("visible");
            if (stateSec != null) {
                return createItemFromSection(player, id, "VISIBILITY", stateSec);
            }
        }

        return createItemFromSection(player, id, action, sec);
    }

    private ItemStack createItemFromSection(Player player, String id, String action, ConfigurationSection sec) {
        String matName = sec.getString("material", "COMPASS");
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.COMPASS;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (mat == Material.PLAYER_HEAD && meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
        }

        String rawName = sec.getString("name", "");
        if (!rawName.isEmpty()) {
            meta.setDisplayName(FileManager.color(rawName.replace("%player%", player.getName())));
        }

        List<String> rawLore = sec.getStringList("lore");
        if (!rawLore.isEmpty()) {
            List<String> lore = new ArrayList<>();
            for (String line : rawLore) {
                lore.add(FileManager.color(line.replace("%player%", player.getName())));
            }
            meta.setLore(lore);
        }

        if (sec.getBoolean("glow", false)) {
            try {
                meta.setEnchantmentGlintOverride(true);
            } catch (Throwable t) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        // Guardar metadata en PDC
        NamespacedKey actionKey = new NamespacedKey(plugin, HOTBAR_PDC_ACTION);
        NamespacedKey idKey = new NamespacedKey(plugin, HOTBAR_PDC_ID);
        meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, id);

        item.setItemMeta(meta);
        return item;
    }

    public void updateVisibilityItem(Player player) {
        if (plugin.getParkourManager() != null && plugin.getParkourManager().isPlaying(player)) return;

        ConfigurationSection itemsSec = plugin.getFiles().getConfig().getConfigurationSection("Hotbar.items");
        if (itemsSec == null) return;

        for (String id : itemsSec.getKeys(false)) {
            ConfigurationSection sec = itemsSec.getConfigurationSection(id);
            if (sec == null) continue;

            String action = sec.getString("action", "");
            if (action.equalsIgnoreCase("VISIBILITY")) {
                int slot = sec.getInt("slot", -1);
                if (slot >= 0 && slot <= 8) {
                    ItemStack newItem = buildHotbarItem(player, id, sec);
                    if (newItem != null) {
                        player.getInventory().setItem(slot, newItem);
                    }
                }
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = e.getItem();
        if (item == null || !item.hasItemMeta()) return;

        NamespacedKey actionKey = new NamespacedKey(plugin, HOTBAR_PDC_ACTION);
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(actionKey, PersistentDataType.STRING)) {
            return;
        }

        String action = meta.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
        if (action == null || action.isEmpty()) return;

        e.setCancelled(true);

        Player player = e.getPlayer();

        // Control de cooldown para evitar spam al dar click
        if (plugin.getCooldownManager().hasCooldown(player, "hotbar")) {
            return;
        }

        double cd = plugin.getFiles().getConfig().getDouble("Cooldowns.HotbarItem", 0.3);
        if (cd > 0) {
            plugin.getCooldownManager().applyCooldown(player, "hotbar", cd);
        }

        plugin.getMenuManager().executeAction(player, action);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (plugin.getBuildMode() != null && plugin.getBuildMode().hasBuildMode(e.getPlayer().getUniqueId())) {
            return;
        }

        ItemStack item = e.getItemDrop().getItemStack();
        if (item.hasItemMeta()) {
            NamespacedKey actionKey = new NamespacedKey(plugin, HOTBAR_PDC_ACTION);
            if (item.getItemMeta().getPersistentDataContainer().has(actionKey, PersistentDataType.STRING)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!isEnabled()) return;
        if (plugin.getFiles().getConfig().getBoolean("Hotbar.give-on-join", true)) {
            giveItems(e.getPlayer());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (!isEnabled()) return;
        if (plugin.getFiles().getConfig().getBoolean("Hotbar.give-on-respawn", true)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (e.getPlayer().isOnline()) {
                        giveItems(e.getPlayer());
                    }
                }
            }.runTaskLater(plugin, 2L);
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        if (!isEnabled()) return;
        if (plugin.getFiles().getConfig().getBoolean("Hotbar.give-on-world-change", true)) {
            giveItems(e.getPlayer());
        }
    }
}