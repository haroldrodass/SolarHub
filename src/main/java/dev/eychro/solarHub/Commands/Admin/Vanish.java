package dev.eychro.solarHub.Commands.Admin;

import dev.eychro.solarHub.SolarHub;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Vanish implements CommandExecutor, Listener {

    private final SolarHub plugin;
    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private final String PERM = "Solar.Vanish";

    public Vanish(SolarHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getFiles().getMessage("OnlyPlayer"));
            return true;
        }

        if (!player.hasPermission(PERM)) {
            player.sendMessage(plugin.getFiles().getMessage("No-permission"));
            return true;
        }

        if (vanishedPlayers.contains(player.getUniqueId())) {
            vanishedPlayers.remove(player.getUniqueId());

            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(plugin, player);
            }

            player.sendMessage(plugin.getFiles().getMessage("Vanished-Disabled"));

        } else {
            vanishedPlayers.add(player.getUniqueId());

            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission(PERM)) {
                    online.hidePlayer(plugin, player);
                }
            }

            player.sendMessage(plugin.getFiles().getMessage("Vanished-Enabled"));
        }

        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player joined = event.getPlayer();

        if (vanishedPlayers.contains(joined.getUniqueId())) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission(PERM)) {
                    online.hidePlayer(plugin, joined);
                }
            }
            joined.sendMessage(plugin.getFiles().getMessage("Vanished-Enabled"));
        }

        if (!joined.hasPermission(PERM)) {
            for (UUID vanishedId : vanishedPlayers) {
                Player vanishedPlayer = Bukkit.getPlayer(vanishedId);
                if (vanishedPlayer != null && vanishedPlayer.isOnline()) {
                    joined.hidePlayer(plugin, vanishedPlayer);
                }
            }
        }
    }

    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }
}