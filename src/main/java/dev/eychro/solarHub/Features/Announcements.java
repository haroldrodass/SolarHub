package dev.eychro.solarHub.Features;

import dev.eychro.solarHub.SolarHub;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Announcements {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final SolarHub plugin;
    private final List<List<String>> announcements = new ArrayList<>();
    private BukkitTask task;
    private int index = 0;

    public Announcements(SolarHub plugin) {
        this.plugin = plugin;
    }

    public void Announce() {
        stop();

        if (!plugin.getFiles().getConfig().getBoolean("Announcements.enabled")) {
            return;
        }

        load();
        if (announcements.isEmpty()) {
            plugin.getLogger().warning("No hay anuncios configurados.");
            return;
        }

        int seconds = Math.max(1, plugin.getFiles().getConfig().getInt("Announcements.timer", 120));
        long ticks = seconds * 20L;

        task = Bukkit.getScheduler().runTaskTimer(plugin, this::broadcastNext, ticks, ticks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void load() {
        announcements.clear();
        index = 0;

        ConfigurationSection section = plugin.getFiles().getMessages().getConfigurationSection("Announcement");
        if (section == null) return;

        for (String category : section.getKeys(false)) {
            List<String> lines = section.getStringList(category);

            boolean hasText = lines.stream().anyMatch(l -> l != null && !l.isBlank());
            if (hasText) {
                announcements.add(lines);
            }
        }
    }

    private void broadcastNext() {
        if (announcements.isEmpty()) return;
        if (index >= announcements.size()) index = 0;

        for (String line : announcements.get(index++)) {
            Bukkit.broadcast(line == null || line.isBlank() ? Component.empty() : parse(line));
        }

        playSound();
    }

    private void playSound() {
        String soundName = plugin.getFiles().getConfig().getString("Announcements.sound", "NONE");

        if (soundName == null || soundName.equalsIgnoreCase("NONE") || soundName.isBlank()) {
            return;
        }

        try {
            String keyString = soundName.toLowerCase().replace("_", ".");
            if (!keyString.contains(":")) {
                keyString = "minecraft:" + keyString;
            }

            Sound adventureSound = Sound.sound(Key.key(keyString), Sound.Source.MASTER, 1.0f, 1.0f);

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(adventureSound);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("El sonido '" + soundName + "' configurado en Announcements.sound no es válido.");
        }
    }

    private Component parse(String line) {
        return line.contains("&") ? LEGACY.deserialize(line) : MM.deserialize(line);
    }
}
