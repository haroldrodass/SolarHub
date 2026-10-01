package dev.eychro.solarHub.Managers;

import dev.eychro.solarHub.SolarHub;
import dev.eychro.solarHub.Utils.RegistryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CosmeticsManager implements Listener {

    private final SolarHub plugin;
    private final Map<UUID, String> activeParticles = new ConcurrentHashMap<>();
    private final Map<UUID, Material> activeHats = new ConcurrentHashMap<>();
    private BukkitTask particleTask;
    private double particleAngle = 0.0;

    public CosmeticsManager(SolarHub plugin) {
        this.plugin = plugin;
        startTask();
    }

    public void startTask() {
        if (particleTask != null) {
            particleTask.cancel();
        }

        particleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            particleAngle += 0.3;
            if (particleAngle > Math.PI * 2) {
                particleAngle = 0;
            }

            for (Map.Entry<UUID, String> entry : activeParticles.entrySet()) {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p == null || !p.isOnline()) {
                    continue;
                }
                spawnParticle(p, entry.getValue());
            }
        }, 2L, 2L);
    }

    public void stopTask() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
    }

    private void spawnParticle(Player player, String type) {
        Location loc = player.getLocation();
        switch (type.toLowerCase()) {
            case "flame" -> {
                // Halo de fuego sobre la cabeza
                double x = 0.4 * Math.cos(particleAngle);
                double z = 0.4 * Math.sin(particleAngle);
                player.getWorld().spawnParticle(Particle.FLAME, loc.clone().add(x, 2.1, z), 1, 0, 0, 0, 0.0);
            }
            case "cloud" -> {
                // Nubes suaves bajo los pies
                player.getWorld().spawnParticle(Particle.CLOUD, loc.clone().add(0, 0.1, 0), 2, 0.15, 0.05, 0.15, 0.01);
            }
            case "hearts" -> {
                // Corazones flotando alrededor
                double x = 0.6 * Math.cos(particleAngle);
                double z = 0.6 * Math.sin(particleAngle);
                player.getWorld().spawnParticle(Particle.HEART, loc.clone().add(x, 1.2, z), 1, 0, 0, 0, 0.0);
            }
            case "magic" -> {
                // Runas de encantamiento hacia el cuerpo
                player.getWorld().spawnParticle(Particle.ENCHANT, loc.clone().add(0, 1.0, 0), 4, 0.3, 0.4, 0.3, 0.1);
            }
            case "portal" -> {
                // Partículas moradas de portal
                player.getWorld().spawnParticle(Particle.PORTAL, loc.clone().add(0, 0.8, 0), 4, 0.3, 0.3, 0.3, 0.1);
            }
            case "totem" -> {
                // Chispas verdes y doradas de tótem
                player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.clone().add(0, 1.2, 0), 3, 0.2, 0.3, 0.2, 0.05);
            }
            default -> {
                Particle p = RegistryUtil.getParticle(type);
                if (p != null) {
                    player.getWorld().spawnParticle(p, loc.clone().add(0, 1.0, 0), 2, 0.2, 0.2, 0.2, 0.01);
                }
            }
        }
    }

    public void setParticle(Player player, String particleId) {
        String current = activeParticles.get(player.getUniqueId());
        if (particleId.equalsIgnoreCase(current)) {
            activeParticles.remove(player.getUniqueId());
            player.sendMessage(plugin.getFiles().getMessage("CosmeticRemoved"));
            playClickSound(player);
            return;
        }

        activeParticles.put(player.getUniqueId(), particleId.toLowerCase());
        player.sendMessage(plugin.getFiles().getMessage("CosmeticActivated", "%cosmetic%", capitalize(particleId)));
        playSuccessSound(player);
    }

    public void setHat(Player player, Material material) {
        Material current = activeHats.get(player.getUniqueId());
        if (current == material) {
            // Desequipar sombrero
            activeHats.remove(player.getUniqueId());
            player.getInventory().setHelmet(null);
            player.sendMessage(plugin.getFiles().getMessage("CosmeticRemoved"));
            playClickSound(player);
            return;
        }

        activeHats.put(player.getUniqueId(), material);
        player.getInventory().setHelmet(new ItemStack(material));
        player.sendMessage(plugin.getFiles().getMessage("CosmeticActivated", "%cosmetic%", formatMaterial(material)));
        playSuccessSound(player);
    }

    public void toggleSpeed(Player player) {
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.removePotionEffect(PotionEffectType.SPEED);
            player.sendMessage(plugin.getFiles().getMessage("CosmeticRemoved"));
            playClickSound(player);
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 1, false, false, false));
            player.sendMessage(plugin.getFiles().getMessage("CosmeticActivated", "%cosmetic%", "Velocidad II"));
            playSuccessSound(player);
        }
    }

    public void toggleJump(Player player) {
        if (player.hasPotionEffect(PotionEffectType.JUMP_BOOST)) {
            player.removePotionEffect(PotionEffectType.JUMP_BOOST);
            player.sendMessage(plugin.getFiles().getMessage("CosmeticRemoved"));
            playClickSound(player);
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, PotionEffect.INFINITE_DURATION, 1, false, false, false));
            player.sendMessage(plugin.getFiles().getMessage("CosmeticActivated", "%cosmetic%", "Salto II"));
            playSuccessSound(player);
        }
    }

    public void clearAll(Player player) {
        boolean hadAny = false;

        if (activeParticles.remove(player.getUniqueId()) != null) hadAny = true;
        if (activeHats.remove(player.getUniqueId()) != null) {
            player.getInventory().setHelmet(null);
            hadAny = true;
        }
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.removePotionEffect(PotionEffectType.SPEED);
            hadAny = true;
        }
        if (player.hasPotionEffect(PotionEffectType.JUMP_BOOST)) {
            player.removePotionEffect(PotionEffectType.JUMP_BOOST);
            hadAny = true;
        }

        if (hadAny) {
            player.sendMessage(plugin.getFiles().getMessage("CosmeticRemoved"));
            playSuccessSound(player);
        } else {
            player.sendMessage(plugin.getFiles().getMessage("NoCosmeticsActive"));
            playClickSound(player);
        }
    }

    public boolean hasParticle(UUID uuid, String id) {
        String p = activeParticles.get(uuid);
        return p != null && p.equalsIgnoreCase(id);
    }

    public boolean hasHat(UUID uuid, Material mat) {
        return activeHats.get(uuid) == mat;
    }

    private void playSuccessSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.4f);
    }

    private void playClickSound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private String formatMaterial(Material mat) {
        String[] parts = mat.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    @EventHandler
    public void onHelmetClick(InventoryClickEvent e) {
        // Evitar que el jugador se quite o cambie el sombrero cosmético desde su inventario si no está en buildmode
        if (e.getSlotType() == InventoryType.SlotType.ARMOR && e.getSlot() == 39) {
            if (activeHats.containsKey(e.getWhoClicked().getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        activeParticles.remove(id);
        if (activeHats.remove(id) != null) {
            e.getPlayer().getInventory().setHelmet(null);
        }
    }
}
