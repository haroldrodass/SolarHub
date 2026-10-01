package dev.eychro.solarHub.Commands.Admin;

import dev.eychro.solarHub.SolarHub;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.UUID;

public class BuildMode implements CommandExecutor {

    private final SolarHub plugin;
    private final HashMap<UUID, BossBar> buildmodelist = new HashMap<>();

    public BuildMode(SolarHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NonNull [] strings) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getFiles().getMessage("OnlyPlayer"));
            return true;
        }

        if (!player.hasPermission("Solar.BuildMode")) {
            player.sendMessage(plugin.getFiles().getMessage("No-permission"));
            return true;
        }

        if (!plugin.getCooldownManager().checkAndApply(player, "buildmode")) {
            return true;
        }

        UUID uuid = player.getUniqueId();

        if (buildmodelist.containsKey(uuid)) {
            BossBar activeBar = buildmodelist.remove(uuid);
            if (activeBar != null) {
                player.hideBossBar(activeBar); // Ocultar del cliente del jugador
            }

            player.sendMessage(plugin.getFiles().getMessage("DisableBuildMode"));
            if (plugin.getHotbarManager() != null) {
                plugin.getHotbarManager().giveItems(player);
            }
        } else {
            Component titulo = Component.text("BUILD MODE").color(NamedTextColor.YELLOW);
            BossBar buildBossBar = BossBar.bossBar(
                    titulo,
                    1.0F,
                    BossBar.Color.YELLOW,
                    BossBar.Overlay.PROGRESS
            );

            buildmodelist.put(uuid, buildBossBar);
            player.showBossBar(buildBossBar);

            player.sendMessage(plugin.getFiles().getMessage("EnableBuildMode"));
        }
        return true;
    }

    public boolean hasBuildMode(UUID uuid) {
        return buildmodelist.containsKey(uuid);
    }

    public void removePlayerOnQuit(Player player) {
        BossBar activeBar = buildmodelist.remove(player.getUniqueId());
        if (activeBar != null) {
            player.hideBossBar(activeBar);
        }
    }
}
