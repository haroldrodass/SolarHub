package dev.eychro.solarHub.Managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FlyManager {

    private final Set<UUID> flyPlayers = new HashSet<>();

    public boolean has(UUID id) {
        return flyPlayers.contains(id);
    }

    public void add(UUID id) {
        flyPlayers.add(id);
    }

    public void remove(UUID id) {
        flyPlayers.remove(id);
    }
}