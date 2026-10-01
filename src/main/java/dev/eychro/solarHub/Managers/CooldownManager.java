package dev.eychro.solarHub.Managers;

import dev.eychro.solarHub.SolarHub;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager implements Listener {

    public static final String BYPASS_PERMISSION = "Solar.CooldownBypass";

    private final SolarHub plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Long>> messageThrottle = new ConcurrentHashMap<>();

    public CooldownManager(SolarHub plugin) {
        this.plugin = plugin;
    }

    public boolean hasBypass(Player player) {
        return player != null && player.hasPermission(BYPASS_PERMISSION);
    }

    public boolean hasCooldown(Player player, String key) {
        if (hasBypass(player)) {
            return false;
        }

        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) {
            return false;
        }

        Long expireTime = playerCooldowns.get(key.toLowerCase());
        if (expireTime == null) {
            return false;
        }

        if (System.currentTimeMillis() >= expireTime) {
            playerCooldowns.remove(key.toLowerCase());
            return false;
        }

        return true;
    }

    public long getRemainingMillis(Player player, String key) {
        if (hasBypass(player)) {
            return 0L;
        }

        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) {
            return 0L;
        }

        Long expireTime = playerCooldowns.get(key.toLowerCase());
        if (expireTime == null) {
            return 0L;
        }

        long remaining = expireTime - System.currentTimeMillis();
        return Math.max(0L, remaining);
    }

    public void applyCooldown(Player player, String key, double durationSeconds) {
        if (hasBypass(player) || durationSeconds <= 0) {
            return;
        }

        long expireAt = System.currentTimeMillis() + (long) (durationSeconds * 1000L);
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(key.toLowerCase(), expireAt);
    }

    public double getCooldownSeconds(String key) {
        FileConfiguration config = plugin.getFiles().getConfig();
        String lowerKey = key.toLowerCase();

        if (config.contains("Cooldowns." + key)) {
            return config.getDouble("Cooldowns." + key);
        }

        if (config.contains("Cooldowns." + lowerKey)) {
            return config.getDouble("Cooldowns." + lowerKey);
        }

        if (config.contains("Cooldowns.Commands." + lowerKey)) {
            return config.getDouble("Cooldowns.Commands." + lowerKey);
        }

        if (lowerKey.equals("doublejump") && config.contains("DoubleJump-Cooldown")) {
            return config.getDouble("DoubleJump-Cooldown");
        }
        if (lowerKey.equals("spawn") && config.contains("Spawn-cooldown")) {
            return config.getDouble("Spawn-cooldown");
        }
        if (lowerKey.equals("launchpad") && config.contains("LaunchPad-Cooldown")) {
            return config.getDouble("LaunchPad-Cooldown");
        }

        if (isCommand(lowerKey)) {
            return config.getDouble("Cooldowns.Commands.default", 0.0);
        }

        return 0.0;
    }

    private boolean isCommand(String key) {
        return key.equals("spawn") || key.equals("discord") || key.equals("website")
                || key.equals("fly") || key.equals("vanish") || key.equals("buildmode")
                || key.equals("setspawn") || key.equals("solarhub")
                || plugin.getCommand(key) != null;
    }

    public boolean checkAndApply(Player player, String key) {
        if (hasCooldown(player, key)) {
            sendCooldownMessage(player, key);
            return false;
        }

        double duration = getCooldownSeconds(key);
        if (duration > 0) {
            applyCooldown(player, key, duration);
        }
        return true;
    }

    public String formatRemaining(long remainingMillis) {
        double seconds = remainingMillis / 1000.0;
        if (seconds >= 60.0) {
            long mins = (long) (seconds / 60);
            long secs = (long) (seconds % 60);
            return mins + "m " + secs + "s";
        }
        if (Math.abs(seconds - Math.round(seconds)) < 0.05) {
            return Math.round(seconds) + "s";
        }
        return String.format(Locale.ROOT, "%.1fs", seconds);
    }

    public void sendCooldownMessage(Player player, String key) {
        long remaining = getRemainingMillis(player, key);
        String formatted = formatRemaining(remaining);
        String secondsCeil = String.valueOf(Math.max(1, (int) Math.ceil(remaining / 1000.0)));

        String msg = plugin.getFiles().getMessage("Cooldown",
                "%cooldown%", formatted,
                "%time%", formatted,
                "%seconds%", secondsCeil,
                "%cooldown_seconds%", secondsCeil
        );
        player.sendMessage(msg);
    }

    public void sendThrottledCooldownMessage(Player player, String key, long throttleMillis) {
        long now = System.currentTimeMillis();
        Map<String, Long> playerThrottles = messageThrottle.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        Long lastSent = playerThrottles.get(key.toLowerCase());

        if (lastSent == null || (now - lastSent) >= throttleMillis) {
            playerThrottles.put(key.toLowerCase(), now);
            sendCooldownMessage(player, key);
        }
    }

    public void removeCooldown(UUID uuid, String key) {
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns != null) {
            playerCooldowns.remove(key.toLowerCase());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cooldowns.remove(e.getPlayer().getUniqueId());
        messageThrottle.remove(e.getPlayer().getUniqueId());
    }
}
