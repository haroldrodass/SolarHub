package dev.eychro.solarHub.Managers;

import dev.eychro.solarHub.SolarHub;
import dev.eychro.solarHub.Utils.RegistryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VisibilityManager implements Listener {

    private final SolarHub plugin;
    private final Set<UUID> hidingPlayers = ConcurrentHashMap.newKeySet();

    public VisibilityManager(SolarHub plugin) {
        this.plugin = plugin;
    }

    public boolean isHidingPlayers(UUID uuid) {
        return hidingPlayers.contains(uuid);
    }

    public void toggleVisibility(Player player) {
        if (hidingPlayers.contains(player.getUniqueId())) {
            hidingPlayers.remove(player.getUniqueId());

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target.equals(player)) continue;
                if (!plugin.getVanish().isVanished(target.getUniqueId())) {
                    player.showPlayer(plugin, target);
                }
            }

            player.sendMessage(plugin.getFiles().getMessage("VisibilityEnabled"));
            playSound(player, "ENTITY_EXPERIENCE_ORB_PICKUP");
        } else {
            hidingPlayers.add(player.getUniqueId());

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target.equals(player)) continue;
                player.hidePlayer(plugin, target);
            }

            player.sendMessage(plugin.getFiles().getMessage("VisibilityDisabled"));
            playSound(player, "ENTITY_EXPERIENCE_ORB_PICKUP");
        }

        if (plugin.getHotbarManager() != null) {
            plugin.getHotbarManager().updateVisibilityItem(player);
        }
    }

    private void playSound(Player player, String defaultSound) {
        String soundName = plugin.getFiles().getConfig().getString("Hotbar.visibility-sound", defaultSound);
        Sound sound = RegistryUtil.getSound(soundName);
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player joining = e.getPlayer();

        for (UUID hidingId : hidingPlayers) {
            Player hidingPlayer = Bukkit.getPlayer(hidingId);
            if (hidingPlayer != null && hidingPlayer.isOnline()) {
                hidingPlayer.hidePlayer(plugin, joining);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        hidingPlayers.remove(e.getPlayer().getUniqueId());
    }
}
