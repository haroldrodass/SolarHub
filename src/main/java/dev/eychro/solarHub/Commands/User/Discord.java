package dev.eychro.solarHub.Commands.User;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Discord implements CommandExecutor {

    private final dev.eychro.solarHub.SolarHub plugin;

    public Discord(dev.eychro.solarHub.SolarHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!plugin.getFiles().getConfig().getBoolean("Discord.Enabled", true)) {
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getFiles().getMessage("OnlyPlayer"));
            return true;
        }

        if (!player.hasPermission("Solar.Discord")) {
            player.sendMessage(plugin.getFiles().getMessage("No-permission"));
            return true;
        }

        if (!plugin.getCooldownManager().checkAndApply(player, "discord")) {
            return true;
        }

        String link = plugin.getFiles().getConfig().getString("Discord.Link", "");

        for (String line : plugin.getFiles().getMessageList("Discord", "%link-discord%", link)) {
            player.sendMessage(line);
        }

        return true;
    }
}
