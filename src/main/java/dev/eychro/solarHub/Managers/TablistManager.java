package dev.eychro.solarHub.Managers;

import dev.eychro.solarHub.SolarHub;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public final class TablistManager {

    private final SolarHub plugin;
    private final boolean hasPlaceholderApi;
    private BukkitTask updateTask;
    private int frame;

    public TablistManager(SolarHub plugin) {
        this.plugin = plugin;
        this.hasPlaceholderApi = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        startTask();
    }

    public void updateAll() {
        if (!isEnabled()) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            update(player);
        }
    }

    public void update(Player player) {
        if (!isEnabled()) {
            return;
        }

        player.setPlayerListHeaderFooter(
                render(player, getFrame("Tablist.header-frames", "Tablist.header", "&6&lSolarHub")),
                render(player, getFrame("Tablist.footer-frames", "Tablist.footer", "&7play.solarhub.net"))
        );

        String prefix = render(player, plugin.getFiles().getConfig().getString("Tablist.rank-prefix", ""));
        String playerListName = prefix.isBlank() ? player.getName() : prefix + player.getName();
        player.setPlayerListName(playerListName);
    }

    private boolean isEnabled() {
        return plugin.getFiles().getConfig().getBoolean("Tablist.enabled", true);
    }

    private String getFrame(String framesPath, String fallbackPath, String defaultValue) {
        List<String> frames = plugin.getFiles().getMessages().getStringList(framesPath);
        if (!frames.isEmpty()) {
            return frames.get(frame % frames.size());
        }
        return plugin.getFiles().getMessages().getString(fallbackPath, defaultValue);
    }

    private String render(Player player, String text) {
        String result = text
                .replace("%player%", player.getName())
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        if (hasPlaceholderApi) {
            result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, result);
        }
        return FileManager.color(result);
    }

    private void startTask() {
        stopTask();
        long interval = Math.max(1L, plugin.getFiles().getConfig().getLong("Tablist.update-interval", 20L));
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                frame++;
                updateAll();
            }
        }.runTaskTimer(plugin, 0L, interval);
    }

    public void reload() {
        startTask();
        updateAll();
    }

    public void stopTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }
}
