package dev.eychro.solarHub;

import dev.eychro.solarHub.Commands.Admin.Reload;
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
        // Plugin shutdown logic
    }

    public void Listeners() {

    }

    public void Commands() {
        getCommand("Reload").setExecutor(new Reload(this));
    }

    public void Files() {
        files = new FileManager(this);
    }

    public FileManager getFiles() {
        return files;
    }

}
