package dev.eychro.solarHub.Commands.Admin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SolarHub implements CommandExecutor {

    private final dev.eychro.solarHub.SolarHub plugin;

    public SolarHub(dev.eychro.solarHub.SolarHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            if (!sender.hasPermission("Solar.Help")) {
                sender.sendMessage(plugin.getFiles().getMessage("No-permission"));
                return true;
            }
            if (sender instanceof org.bukkit.entity.Player player) {
                if (!plugin.getCooldownManager().checkAndApply(player, "solarhub")) {
                    return true;
                }
            }
            for (String line : plugin.getFiles().getMessageList("Help")) {
                sender.sendMessage(line);
            }
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("Solar.Reload")) {
                sender.sendMessage(plugin.getFiles().getMessage("No-permission"));
                return true;
            }
            if (sender instanceof org.bukkit.entity.Player player) {
                if (!plugin.getCooldownManager().checkAndApply(player, "solarhub")) {
                    return true;
                }
            }
            plugin.reloadAll();
            sender.sendMessage(plugin.getFiles().getMessage("Reloaded"));
            return true;
        }
        return false;
    }
}