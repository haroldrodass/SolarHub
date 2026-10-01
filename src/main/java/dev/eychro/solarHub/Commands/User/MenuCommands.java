package dev.eychro.solarHub.Commands.User;

import dev.eychro.solarHub.SolarHub;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MenuCommands implements CommandExecutor {

    private final SolarHub plugin;
    private final String menuId;
    private final String permission;

    public MenuCommands(SolarHub plugin, String menuId, String permission) {
        this.plugin = plugin;
        this.menuId = menuId;
        this.permission = permission;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getFiles().getMessage("OnlyPlayer"));
            return true;
        }

        if (permission != null && !permission.isEmpty() && !player.hasPermission(permission)) {
            player.sendMessage(plugin.getFiles().getMessage("No-permission"));
            return true;
        }

        if (!plugin.getCooldownManager().checkAndApply(player, command.getName().toLowerCase())) {
            return true;
        }

        plugin.getMenuManager().openMenu(player, menuId);
        return true;
    }
}
