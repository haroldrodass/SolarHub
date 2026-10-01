package dev.eychro.solarHub.Commands.User;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Website implements CommandExecutor {

    private final dev.eychro.solarHub.SolarHub plugin;

    public Website(dev.eychro.solarHub.SolarHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!plugin.getFiles().getConfig().getBoolean("Website.Enabled", true)) {
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getFiles().getMessage("OnlyPlayer"));
            return true;
        }

        if (!player.hasPermission("Solar.Website")) {
            player.sendMessage(plugin.getFiles().getMessage("No-permission"));
            return true;
        }

        if (!plugin.getCooldownManager().checkAndApply(player, "website")) {
            return true;
        }

        String link = plugin.getFiles().getConfig().getString("Website.Link", "");

        for (String line : plugin.getFiles().getMessageList("Website", "%link-website%", link)) {
            player.sendMessage(line);
        }

        return true;
    }
}
