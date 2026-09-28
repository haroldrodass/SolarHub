package dev.eychro.solarHub.Commands.Admin;

import dev.eychro.solarHub.SolarHub;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.UUID;

public class BuildMode implements CommandExecutor {

    private final SolarHub plugin;
    private final HashSet<UUID> buildmodelist = new HashSet<>();

    public BuildMode(SolarHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NonNull [] strings) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfig().getString("Messages.OnlyPlayer"));
            return true;
        }

        if (!player.hasPermission("Solar.BuildMode")) {
            sender.sendMessage(plugin.getConfig().getString("Messages.NoPermission"));
        }

        if (buildmodelist.contains(player.getUniqueId())) {
            buildmodelist.remove(player.getUniqueId());
            player.sendMessage(plugin.getFiles().getMessage("DisableBuildMode"));
        } else {
            buildmodelist.add(player.getUniqueId());
            player.sendMessage(plugin.getFiles().getMessage("EnableBuildMode"));
        }
        return true;
    }

    public boolean hasBuildMode(UUID uuid) {
        return buildmodelist.contains(uuid);
    }
}