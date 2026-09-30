package dev.eychro.solarHub.Features;

import dev.eychro.solarHub.SolarHub;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Duration;

public class Welcome implements Listener {

    private final SolarHub plugin;

    public Welcome(SolarHub plugin) {
        this.plugin = plugin;
    }

    private Title.Times loadTimes() {
        var cfg = plugin.getFiles().getConfig();
        return Title.Times.times(
                Duration.ofMillis((long) (cfg.getDouble("Welcome-Title.Time.Appearance", 0.5) * 1000)),
                Duration.ofMillis((long) (cfg.getDouble("Welcome-Title.Time.Duration", 3) * 1000)),
                Duration.ofMillis((long) (cfg.getDouble("Welcome-Title.Time.fading", 0.5) * 1000))
        );
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!plugin.getFiles().getConfig().getBoolean("Welcome-Title.enabled", true)) return;

        Player player = e.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            var name = Placeholder.unparsed("player", player.getName());

            player.showTitle(Title.title(
                    plugin.getFiles().getComponent("Welcome-Title", name),
                    plugin.getFiles().getComponent("Welcome-Subtitle", name),
                    loadTimes()
            ));
        }, 10L);
    }
}