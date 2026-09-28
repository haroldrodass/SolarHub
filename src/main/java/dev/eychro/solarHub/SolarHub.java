package dev.eychro.solarHub;

import dev.eychro.solarHub.Commands.User.Discord;
import dev.eychro.solarHub.Commands.User.Website;
import dev.eychro.solarHub.Managers.FileManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SolarHub extends JavaPlugin {

    private FileManager files;

    @Override
    public void onEnable() {
        Files();
        Commands();
        Listeners();

    }

    @Override
    public void onDisable() {

    }

    public void Listeners() {

    }

    public void Commands() {
        getCommand("Reload").setExecutor(new dev.eychro.solarHub.Commands.Admin.SolarHub(this));
        getCommand("Discord").setExecutor(new Discord(this));
        getCommand("Website").setExecutor(new Website(this));
    }

    public void reloadAll() {
        files.reloadAll();
    }

    public void Files() {
        files = new FileManager(this);
    }

    public FileManager getFiles() {
        return files;
    }

}
