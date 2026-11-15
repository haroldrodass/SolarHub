package dev.eychro.solarHub.Managers;

import dev.eychro.solarHub.SolarHub;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TablistManager {

    private final SolarHub plugin;
    private final boolean hasPlaceholderApi;
    private final TablistLayoutManager layoutManager;
    private BukkitTask updateTask;
    private int frame;

    public TablistManager(SolarHub plugin) {
        this.plugin = plugin;
        this.hasPlaceholderApi = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        this.layoutManager = new TablistLayoutManager(plugin);
        startTask();
    }

    public void updateAll() {
        if (!isEnabled()) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            update(player);
        }
        layoutManager.updateAll();
    }

    public void update(Player player) {
        if (!isEnabled()) {
            return;
        }

        player.setPlayerListHeaderFooter(
                render(player, getConfiguredText("header", "Tablist.header-frames", "Tablist.header", "&6&lSolarHub")),
                render(player, getConfiguredText("footer", "Tablist.footer-frames", "Tablist.footer", "&7play.solarhub.net"))
        );

        String prefix = render(player, plugin.getFiles().getConfig().getString("Tablist.rank-prefix", ""));
        String playerListFormat = plugin.getFiles().getConfig().getString("Tablist.player-list-name", "");
        String playerListName = playerListFormat.isBlank()
                ? (prefix.isBlank() ? player.getName() : prefix + player.getName())
                : render(player, playerListFormat.replace("%rank_prefix%", prefix));
        player.setPlayerListName(playerListName);
    }

    public void onJoin(Player player) {
        if (!isEnabled()) {
            return;
        }
        update(player);
        layoutManager.update(player);
        // Vanilla lists the new player to everyone on join; hide them again right after.
        Bukkit.getScheduler().runTaskLater(plugin, layoutManager::updateAll, 2L);
    }

    public void onQuit(Player player) {
        layoutManager.clear(player);
    }

    public TablistLayoutManager getLayoutManager() {
        return layoutManager;
    }

    private boolean isEnabled() {
        return plugin.getFiles().getConfig().getBoolean("Tablist.enabled", true);
    }

    private String getConfiguredText(String section, String legacyFramesPath, String legacyPath, String defaultValue) {
        List<String> frames = plugin.getFiles().getConfig().getStringList("Tablist." + section + "-frames");
        if (!frames.isEmpty()) {
            return frames.get(frame % frames.size());
        }

        List<String> lines = plugin.getFiles().getConfig().getStringList("Tablist." + section);
        if (!lines.isEmpty()) {
            return String.join("\n", lines);
        }

        frames = plugin.getFiles().getMessages().getStringList(legacyFramesPath);
        if (!frames.isEmpty()) {
            return frames.get(frame % frames.size());
        }
        return plugin.getFiles().getMessages().getString(legacyPath, defaultValue);
    }

    private String render(Player player, String text) {
        String result = text
                .replace("%player%", player.getName())
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%max_players%", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("%ping%", String.valueOf(player.getPing()))
                .replace("%world%", player.getWorld().getName());

        if (plugin.getServerStatusManager() != null) {
            Pattern serverPattern = Pattern.compile("%server_players_([a-zA-Z0-9_-]+)%");
            Matcher m = serverPattern.matcher(result);
            StringBuilder sb = new StringBuilder();
            while (m.find()) {
                String srv = m.group(1);
                int count = plugin.getServerStatusManager().getPlayerCount(srv);
                m.appendReplacement(sb, count >= 0 ? String.valueOf(count) : "0");
            }
            m.appendTail(sb);
            result = sb.toString();
        }

        if (hasPlaceholderApi) {
            result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, result);
        }

        String rank = "";
        if (hasPlaceholderApi) {
            rank = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, "%luckperms_prefix%");
        }
        if (rank == null || rank.isBlank() || rank.equals("%luckperms_prefix%")) {
            rank = "Default";
        }
        result = result.replace("%rank%", rank).replace("%luckperms_prefix%", rank);

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
        layoutManager.reloadSkin();
        startTask();
        updateAll();
    }

    public void stopTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        layoutManager.clearAll();
    }
}