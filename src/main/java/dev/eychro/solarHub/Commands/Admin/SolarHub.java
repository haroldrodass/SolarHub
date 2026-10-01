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

        if (args.length >= 2 && args[0].equalsIgnoreCase("menu")) {
            if (!sender.hasPermission("Solar.Admin")) {
                sender.sendMessage(plugin.getFiles().getMessage("No-permission"));
                return true;
            }
            if (!(sender instanceof org.bukkit.entity.Player player)) {
                sender.sendMessage(plugin.getFiles().getMessage("OnlyPlayer"));
                return true;
            }
            plugin.getMenuManager().openMenu(player, args[1]);
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("giveitems")) {
            if (!sender.hasPermission("Solar.Admin")) {
                sender.sendMessage(plugin.getFiles().getMessage("No-permission"));
                return true;
            }
            org.bukkit.entity.Player target = null;
            if (args.length >= 2) {
                target = org.bukkit.Bukkit.getPlayer(args[1]);
            } else if (sender instanceof org.bukkit.entity.Player p) {
                target = p;
            }

            if (target == null) {
                sender.sendMessage("§cJugador no encontrado.");
                return true;
            }

            plugin.getHotbarManager().giveItems(target);
            sender.sendMessage("§aObjetos de hotbar entregados a " + target.getName() + ".");
            return true;
        }

        sender.sendMessage("§cUso: /solarhub <help|reload|menu <nombre>|giveitems [jugador]>");
        return true;
    }
}