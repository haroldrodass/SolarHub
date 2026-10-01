package dev.eychro.solarHub.Commands.Admin;

import dev.eychro.solarHub.Managers.FileManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetSpawn implements CommandExecutor {

    private final dev.eychro.solarHub.SolarHub plugin;

    public SetSpawn(dev.eychro.solarHub.SolarHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getFiles().getMessage("OnlyPlayer"));
            return true;
        }

        if (!player.hasPermission("Solar.SetSpawn")) {
            player.sendMessage(plugin.getFiles().getMessage("No-permission"));
            return true;
        }

        if (!plugin.getCooldownManager().checkAndApply(player, "setspawn")) {
            return true;
        }

        Location loc = player.getLocation();
        FileConfiguration config = plugin.getFiles().getConfig();
        config.set("Spawn.Set", true);
        config.set("Spawn.World", loc.getWorld().getName());
        config.set("Spawn.X", loc.getX());
        config.set("Spawn.Y", loc.getY());
        config.set("Spawn.Z", loc.getZ());
        config.set("Spawn.Yaw", (double) loc.getYaw());
        config.set("Spawn.Pitch", (double) loc.getPitch());
        plugin.getFiles().save(FileManager.CONFIG);

        player.sendMessage(plugin.getFiles().getMessage("SpawnSet"));

        return true;
    }
}
