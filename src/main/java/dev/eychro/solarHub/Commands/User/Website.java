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
        if (!plugin.getFiles().getConfig().getBoolean("Website.Enabled")) {
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfig().getString("Messages.OnlyPlayer"));
            return true;
        }

        if (!sender.hasPermission("Solar.Discord")) {
            sender.sendMessage(plugin.getConfig().getString("Messages.NoPermission"));
            return true;
        }

        String Link = plugin.getFiles().getConfig().getString("Website.Link");

        for (String line : plugin.getFiles().getMessageList("Website", "%link-website%", Link)) {
            sender.sendMessage(line);
        }

        return false;
    }
}
