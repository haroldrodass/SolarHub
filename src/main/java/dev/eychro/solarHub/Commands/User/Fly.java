package dev.eychro.solarHub.Commands.User;

import dev.eychro.solarHub.SolarHub;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Fly implements CommandExecutor {

    private final SolarHub plugin;

    public Fly(SolarHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getFiles().getMessage("OnlyPlayer"));
            return true;
        }

        if (!player.hasPermission("Solar.Fly")) {
            player.sendMessage(plugin.getFiles().getMessage("No-permission"));
            return true;
        }

        if (!plugin.getCooldownManager().checkAndApply(player, "fly")) {
            return true;
        }

        if (plugin.getFlyManager().has(player.getUniqueId())) {
            plugin.getFlyManager().remove(player.getUniqueId());
            player.setFlying(false);
            player.setAllowFlight(false);
            player.sendMessage(plugin.getFiles().getMessage("FlyDisabled"));
        } else {
            plugin.getFlyManager().add(player.getUniqueId());
            player.setAllowFlight(true);
            player.sendMessage(plugin.getFiles().getMessage("FlyEnabled"));
        }

        return true;
    }
}