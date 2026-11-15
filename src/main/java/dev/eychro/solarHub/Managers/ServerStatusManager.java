package dev.eychro.solarHub.Managers;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.eychro.solarHub.SolarHub;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerStatusManager implements PluginMessageListener {

    private final SolarHub plugin;
    private final Map<String, Integer> playerCounts = new ConcurrentHashMap<>();
    private final Set<String> knownServers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, String> currentServers = new ConcurrentHashMap<>();

    public ServerStatusManager(SolarHub plugin) {
        this.plugin = plugin;
    }

    public void requestPlayerCount(Player player, String serverName) {
        if (serverName == null || serverName.isBlank()) {
            return;
        }

        sendRequest(player, "PlayerCount", serverName);
    }

    public void requestCurrentServer(Player player) {
        sendRequest(player, "GetServer", null);
    }

    public String getCurrentServer(Player player) {
        return currentServers.get(player.getUniqueId());
    }

    public int getPlayerCount(String serverName) {
        return playerCounts.getOrDefault(serverName.toLowerCase(), -1);
    }

    public String getStatus(String serverName) {
        return getStatus(serverName, -1);
    }

    public String getStatus(String serverName, int maxPlayers) {
        if (serverName == null || serverName.isBlank()) {
            return "offline";
        }

        int playerCount = getPlayerCount(serverName);
        if (!knownServers.contains(serverName.toLowerCase()) || playerCount < 0) {
            return "offline";
        }
        if (maxPlayers > 0 && playerCount >= maxPlayers) {
            return "full";
        }
        return "online";
    }

    private void sendRequest(Player player, String subChannel, String serverName) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF(subChannel);
        if (serverName != null) {
            output.writeUTF(serverName);
        }
        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!"BungeeCord".equals(channel)) {
            return;
        }

        ByteArrayDataInput input = ByteStreams.newDataInput(message);
        String subChannel = input.readUTF();
        if ("PlayerCount".equalsIgnoreCase(subChannel)) {
            String serverName = input.readUTF();
            int count = input.readInt();
            String key = serverName.toLowerCase();
            knownServers.add(key);
            playerCounts.put(key, count);
        } else if ("GetServers".equalsIgnoreCase(subChannel)) {
            for (String server : input.readUTF().split(", ")) {
                if (!server.isBlank()) {
                    knownServers.add(server.toLowerCase());
                }
            }
        } else if ("GetServer".equalsIgnoreCase(subChannel)) {
            String currentServer = input.readUTF();
            String previousServer = currentServers.put(player.getUniqueId(), currentServer.toLowerCase());
            if (!currentServer.equalsIgnoreCase(previousServer == null ? "" : previousServer)) {
                plugin.getMenuManager().refreshMenuIfOpen(player, "lobbies");
            }
        }
    }
}
