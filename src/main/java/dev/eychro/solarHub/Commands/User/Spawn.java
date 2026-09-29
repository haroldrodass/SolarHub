package dev.eychro.solarHub.Commands.User;

import dev.eychro.solarHub.Listeners.Player.JoinListener;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Spawn implements CommandExecutor {

    private final dev.eychro.solarHub.SolarHub plugin;

    public Spawn(dev.eychro.solarHub.SolarHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getFiles().getMessage("OnlyPlayer"));
            return true;
        }

        if (!sender.hasPermission("Solar.Spawn")) {
            sender.sendMessage(plugin.getFiles().getMessage("No-permission"));
            return true;
        }

        if (JoinListener.getSpawn(plugin) == null) {
            sender.sendMessage(plugin.getFiles().getMessage("No-Spawn"));
        } else {
            ((Player) sender).teleport(JoinListener.getSpawn(plugin));
        }
        return true;
    }
}
