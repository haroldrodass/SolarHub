package dev.eychro.solarHub.Features;

import dev.eychro.solarHub.SolarHub;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Particle;
import org.bukkit.entity.Firework;
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
        Player player = e.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            if (plugin.getFiles().getConfig().getBoolean("Welcome-Title.enabled", true)) {
                var name = Placeholder.unparsed("player", player.getName());

                player.showTitle(Title.title(
                        plugin.getFiles().getComponent("Welcome-Title", name),
                        plugin.getFiles().getComponent("Welcome-Subtitle", name),
                        loadTimes()
                ));
            }

            playJoinEffects(player);
        }, 10L);
    }

    private void playJoinEffects(Player player) {
        var config = plugin.getFiles().getConfig();
        if (!config.getBoolean("Welcome-Effects.enabled", false)) {
            return;
        }

        player.getWorld().spawnParticle(
                Particle.HAPPY_VILLAGER,
                player.getLocation().add(0, 1, 0),
                10,
                0.5,
                0.5,
                0.5,
                0.1
        );
        if (!config.getBoolean("Welcome-Effects.firework.enabled", true)) {
            return;
        }

        Firework firework = player.getWorld().spawn(player.getLocation().add(0, 1, 0), Firework.class);
        var meta = firework.getFireworkMeta();
        meta.setPower(0);
        meta.addEffect(FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL)
                .withColor(Color.ORANGE, Color.YELLOW)
                .flicker(true)
                .trail(true)
                .build());
        firework.setFireworkMeta(meta);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!firework.isDead()) {
                firework.detonate();
            }
        }, Math.max(1L, config.getLong("Welcome-Effects.firework.detonate-after", 2L)));
    }
}