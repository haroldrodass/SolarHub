package dev.eychro.solarHub.Utils;

import dev.eychro.solarHub.SolarHub;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Registers %solarhub_*% placeholders with PlaceholderAPI.
 * Only loaded when PlaceholderAPI is present on the server.
 *
 * Available placeholders:
 *   %solarhub_ping%              - Player's ping in ms
 *   %solarhub_fly%               - Fly status (Activado / Desactivado)
 *   %solarhub_vanish%            - Vanish status (Activado / Desactivado)
 *   %solarhub_visibility%        - Visibility status (Visibles / Ocultos)
 *   %solarhub_parkour_playing%   - Whether the player is in parkour (Sí / No)
 *   %solarhub_parkour_time%      - Current elapsed parkour time (or "--:--")
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final SolarHub plugin;

    public PlaceholderAPIHook(SolarHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "solarhub";
    }

    @Override
    public @NotNull String getAuthor() {
        return "eychro";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        return switch (identifier.toLowerCase()) {
            case "ping" -> String.valueOf(player.getPing());

            case "fly" -> player.isFlying() || player.getAllowFlight() ? "§aActivado" : "§cDesactivado";

            case "vanish" -> {
                boolean vanished = plugin.getVanish() != null
                        && plugin.getVanish().isVanished(player.getUniqueId());
                yield vanished ? "§aActivado" : "§cDesactivado";
            }

            case "visibility" -> {
                boolean hidden = plugin.getVisibilityManager() != null
                        && plugin.getVisibilityManager().isHidingPlayers(player.getUniqueId());
                yield hidden ? "§cOcultos" : "§aVisibles";
            }

            case "parkour_playing" -> {
                boolean playing = plugin.getParkourManager() != null
                        && plugin.getParkourManager().isPlaying(player);
                yield playing ? "§aSí" : "§cNo";
            }

            case "parkour_time" -> {
                if (plugin.getParkourManager() == null || !plugin.getParkourManager().isPlaying(player)) {
                    yield "--:--.--";
                }
                long start = plugin.getParkourManager().getStartTime(player);
                long elapsed = System.currentTimeMillis() - start;
                yield plugin.getParkourManager().formatTime(elapsed);
            }

            default -> null;
        };
    }
}
