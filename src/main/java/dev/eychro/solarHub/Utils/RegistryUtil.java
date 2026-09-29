package dev.eychro.solarHub.Utils;

import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;

public final class RegistryUtil {

    private RegistryUtil() {
    }

    private static boolean isNone(String name) {
        return name == null || name.isEmpty() || name.equalsIgnoreCase("NONE");
    }


    private static String normalize(String s) {
        return s.toLowerCase().replace("_", "").replace(".", "");
    }

    public static Sound getSound(String name) {
        if (isNone(name)) {
            return null;
        }

        String lower = name.trim().toLowerCase();

        NamespacedKey key = NamespacedKey.fromString(lower);
        if (key != null) {
            Sound s = Registry.SOUNDS.get(key);
            if (s != null) {
                return s;
            }
        }

        key = NamespacedKey.fromString(lower.replace('_', '.'));
        if (key != null) {
            Sound s = Registry.SOUNDS.get(key);
            if (s != null) {
                return s;
            }
        }

        String normalized = normalize(lower);
        for (Sound candidate : Registry.SOUNDS) {
            NamespacedKey candidateKey = Registry.SOUNDS.getKey(candidate);
            if (candidateKey != null && normalize(candidateKey.getKey()).equals(normalized)) {
                return candidate;
            }
        }

        return null;
    }

    public static Particle getParticle(String name) {
        if (isNone(name)) {
            return null;
        }

        String lower = name.trim().toLowerCase();

        NamespacedKey key = NamespacedKey.fromString(lower);
        if (key != null) {
            Particle p = Registry.PARTICLE_TYPE.get(key);
            if (p != null) {
                return p;
            }
        }

        String normalized = normalize(lower);
        for (Particle candidate : Registry.PARTICLE_TYPE) {
            NamespacedKey candidateKey = Registry.PARTICLE_TYPE.getKey(candidate);
            if (candidateKey != null && normalize(candidateKey.getKey()).equals(normalized)) {
                return candidate;
            }
        }

        return null;
    }
}