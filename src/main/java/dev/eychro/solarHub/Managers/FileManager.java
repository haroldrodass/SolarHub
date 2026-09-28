package dev.eychro.solarHub.Managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileManager {

    private final JavaPlugin plugin;

    private final Map<String, File> files = new HashMap<>();
    private final Map<String, FileConfiguration> configs = new HashMap<>();

    public static final String CONFIG = "config.yml";
    public static final String MESSAGES = "messages.yml";

    public FileManager(JavaPlugin plugin) {
        this.plugin = plugin;
        setup(CONFIG);
        setup(MESSAGES);
    }

    private void setup(String name) {
        File file = new File(plugin.getDataFolder(), name);

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!file.exists()) {
            plugin.saveResource(name, false);
        }

        files.put(name, file);
        configs.put(name, YamlConfiguration.loadConfiguration(file));
    }

    public FileConfiguration get(String name) {
        return configs.get(name);
    }

    public FileConfiguration getConfig() {
        return get(CONFIG);
    }

    public FileConfiguration getMessages() {
        return get(MESSAGES);
    }

    public void save(String name) {
        try {
            configs.get(name).save(files.get(name));
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo guardar " + name + ": " + e.getMessage());
        }
    }

    public void reload(String name) {
        File file = files.get(name);
        if (file == null || !file.exists()) {
            setup(name);
            return;
        }
        configs.put(name, YamlConfiguration.loadConfiguration(file));
    }

    public void reloadAll() {
        reload(CONFIG);
        reload(MESSAGES);
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public String getMessage(String path) {
        String prefix = getMessages().getString("prefix", "");
        String msg = getMessages().getString(path, "&cMensaje no encontrado: " + path);
        return color(prefix + msg);
    }

    public String getMessage(String path, String... replacements) {
        String msg = getMessage(path);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        return msg;
    }

    public List<String> getMessageList(String path, String... replacements) {
        List<String> result = new ArrayList<>();

        for (String line : getMessages().getStringList(path)) {
            for (int i = 0; i + 1 < replacements.length; i += 2) {
                line = line.replace(replacements[i], replacements[i + 1]);
            }
            result.add(color(line));
        }
        return result;
    }
}