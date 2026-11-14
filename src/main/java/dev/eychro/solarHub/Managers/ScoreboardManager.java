package dev.eychro.solarHub.Managers;

import dev.eychro.solarHub.SolarHub;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ScoreboardManager — sidebar scoreboard per player.
 *
 * Config.yml:
 *   Scoreboard:
 *     enabled: true
 *     update-interval: 20   # ticks between updates (20 = 1 second)
 *
 * Messages.yml:
 *   Scoreboard:
 *     title: "&6&lSolar Hub"
 *     lines:
 *       - "&7%player%"
 *       - "&7Ping: &a%solarhub_ping%ms"
 *       ...
 *
 * Supports %solarhub_*% placeholders if PlaceholderAPI is installed.
 */
public class ScoreboardManager {

    private static final String OBJECTIVE_NAME = "solarhub_sb";

    private final SolarHub plugin;
    private final Map<UUID, org.bukkit.scoreboard.Scoreboard> scoreboards = new HashMap<>();
    private int titleFrame;
    private BukkitTask task;

    private final boolean hasPAPI;

    public ScoreboardManager(SolarHub plugin) {
        this.plugin = plugin;
        this.hasPAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (isEnabled()) {
            startTask();
        }
    }

    public boolean isEnabled() {
        return plugin.getFiles().getConfig().getBoolean("Scoreboard.enabled", true);
    }


    public void showScoreboard(Player player) {
        if (!isEnabled()) return;
        org.bukkit.scoreboard.Scoreboard scoreboard = buildScoreboard(player);
        scoreboards.put(player.getUniqueId(), scoreboard);
        player.setScoreboard(scoreboard);
    }

    public void removeScoreboard(Player player) {
        scoreboards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (scoreboards.containsKey(player.getUniqueId())) {
                updateScoreboard(player);
            }
        }
    }

    private org.bukkit.scoreboard.Scoreboard buildScoreboard(Player player) {
        org.bukkit.scoreboard.Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = sb.registerNewObjective(
                OBJECTIVE_NAME,
                "dummy",
                net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                        .legacySection().deserialize(colorize(player, getTitle()))
        );
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat.blank());
        updateLines(player, sb, obj);

        return sb;
    }

    private void updateScoreboard(Player player) {
        org.bukkit.scoreboard.Scoreboard sb = scoreboards.get(player.getUniqueId());
        if (sb == null) return;

        Objective obj = sb.getObjective(OBJECTIVE_NAME);
        if (obj == null) return;

        String title = colorize(player, getTitle());
        obj.displayName(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                .legacySection().deserialize(title));
        obj.numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat.blank());
        updateLines(player, sb, obj);
    }

    private void updateLines(Player player, org.bukkit.scoreboard.Scoreboard scoreboard, Objective objective) {
        for (String entry : new ArrayList<>(scoreboard.getEntries())) {
            scoreboard.resetScores(entry);
        }

        List<String> lines = buildLines(player);
        int score = lines.size();
        for (String line : lines) {
            String entry = uniqueEntry(line, score, scoreboard);
            objective.getScore(entry).setScore(score--);
        }
    }

    private String uniqueEntry(String line, int index, org.bukkit.scoreboard.Scoreboard scoreboard) {
        String value = line == null ? "" : line;
        if (value.length() > 40) {
            value = value.substring(0, 40);
        }

        String suffix = ChatColor.values()[index % ChatColor.values().length].toString();
        String entry = value + suffix;
        while (scoreboard.getEntries().contains(entry)) {
            entry += ChatColor.RESET;
        }
        return entry;
    }

    private String getTitle() {
        List<String> frames = plugin.getFiles().getMessages().getStringList("Scoreboard.title-frames");
        if (!frames.isEmpty()) {
            return frames.get(titleFrame % frames.size());
        }
        return plugin.getFiles().getMessages().getString("Scoreboard.title", "&6&lSolar Hub");
    }

    private List<String> buildLines(Player player) {
        List<?> raw = plugin.getFiles().getMessages().getList("Scoreboard.lines");
        List<String> result = new ArrayList<>();
        if (raw == null) return result;
        for (Object o : raw) {
            String line = o == null ? "" : o.toString();
            line = colorize(player, line);
            result.add(line);
        }
        return result;
    }

    private String colorize(Player player, String text) {
        text = text
                .replace("%player%", player.getName())
                .replace("%ping%", String.valueOf(player.getPing()))
                .replace("%world%", player.getWorld().getName())
                .replace("%fly_status%", (player.isFlying() || player.getAllowFlight()) ? "§aActivado" : "§cDesactivado")
                .replace("%vanish_status%", plugin.getVanish() != null && plugin.getVanish().isVanished(player.getUniqueId()) ? "§aActivado" : "§cDesactivado")
                .replace("%visibility_status%", plugin.getVisibilityManager() != null && plugin.getVisibilityManager().isHidingPlayers(player.getUniqueId()) ? "§cOcultos" : "§aVisibles")
                .replace("%parkour_playing%", plugin.getParkourManager() != null && plugin.getParkourManager().isPlaying(player) ? "§aSí" : "§cNo");

        if (hasPAPI) {
            text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }


    private void startTask() {
        if (task != null) task.cancel();
        int interval = plugin.getFiles().getConfig().getInt("Scoreboard.update-interval", 20);
        task = new BukkitRunnable() {
            @Override
            public void run() {
                titleFrame++;
                updateAll();
            }
        }.runTaskTimer(plugin, interval, interval);
    }

    public void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }


    public void reload() {
        stopTask();
        if (isEnabled()) {
            startTask();
            for (Player player : Bukkit.getOnlinePlayers()) {
                showScoreboard(player);
            }
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                removeScoreboard(player);
            }
        }
    }
}
