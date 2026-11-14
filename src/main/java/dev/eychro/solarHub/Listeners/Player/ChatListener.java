package dev.eychro.solarHub.Listeners.Player;

import dev.eychro.solarHub.SolarHub;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class ChatListener implements Listener {

    private final SolarHub plugin;

    public ChatListener(SolarHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!plugin.getFiles().getConfig().getBoolean("Chat.enabled", true)) {
            return;
        }

        if (!plugin.getCooldownManager().checkAndApply(event.getPlayer(), "chat")) {
            event.setCancelled(true);
            return;
        }

        String format = plugin.getFiles().getMessages().getString(
                "Chat.format",
                "&7%player% &8» &f%message%"
        );
        format = format.replace("%player%", event.getPlayer().getName());
        format = format.replace("%message%", "%2$s");
        event.setFormat(org.bukkit.ChatColor.translateAlternateColorCodes('&', format));
    }
}
