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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class FileManager {

    public static final String CONFIG = "Config.yml";
    public static final String MESSAGES = "Messages.yml";
    private final JavaPlugin plugin;
    private final Map<String, File> files = new HashMap<>();
    private final Map<String, FileConfiguration> configs = new HashMap<>();

    public FileManager(JavaPlugin plugin) {
        this.plugin = plugin;
        setup(CONFIG);
        setup(MESSAGES);
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
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

    public String getMessage(String path) {
        String prefix = getMessages().getString("Prefix", "");
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

    private static final MiniMessage MM = MiniMessage.miniMessage();

    public String getRaw(String path) {
        return getMessages().getString(path, "<red>Mensaje no encontrado: " + path);
    }

    public Component getComponent(String path, TagResolver... resolvers) {
        return MM.deserialize(getRaw(path), resolvers);
    }


}