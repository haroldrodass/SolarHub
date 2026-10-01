package dev.eychro.solarHub.Listeners.Player;

import dev.eychro.solarHub.SolarHub;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final SolarHub plugin;

    public JoinListener(SolarHub plugin) {
        this.plugin = plugin;
    }

    public static Location getSpawn(SolarHub plugin) {
        FileConfiguration config = plugin.getFiles().getConfig();

        if (!config.getBoolean("Spawn.Set", false)) {
            return null;
        }

        World world = Bukkit.getWorld(config.getString("Spawn.World", "world"));
        if (world == null) {
            return null;
        }

        return new Location(world,
                config.getDouble("Spawn.X"),
                config.getDouble("Spawn.Y"),
                config.getDouble("Spawn.Z"),
                (float) config.getDouble("Spawn.Yaw"),
                (float) config.getDouble("Spawn.Pitch"));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Location spawn = getSpawn(plugin);
        if (spawn != null) {
            e.getPlayer().teleport(spawn);
        }
        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().showScoreboard(e.getPlayer());
        }
    }
}