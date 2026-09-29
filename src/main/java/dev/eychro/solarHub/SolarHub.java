package dev.eychro.solarHub;

import dev.eychro.solarHub.Commands.Admin.BuildMode;
import dev.eychro.solarHub.Commands.Admin.SetSpawn;
import dev.eychro.solarHub.Commands.User.Discord;
import dev.eychro.solarHub.Commands.User.Spawn;
import dev.eychro.solarHub.Commands.User.Website;
import dev.eychro.solarHub.Features.DoubleJump;
import dev.eychro.solarHub.Listeners.Player.JoinListener;
import dev.eychro.solarHub.Listeners.Player.JoinMessage;
import dev.eychro.solarHub.Listeners.World.BlockInteractions;
import dev.eychro.solarHub.Listeners.World.ItemProtection;
import dev.eychro.solarHub.Listeners.Player.PlayerProtection;
import dev.eychro.solarHub.Managers.FileManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class SolarHub extends JavaPlugin {

    private FileManager files;
    private BuildMode buildMode;

    @Override
    public void onEnable() {
        Files();
        Commands();
        Listeners();

    }

    @Override
    public void onDisable() {

    }

    private void register(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public void Listeners() {
        register(new BlockInteractions(this, buildMode));
        register(new PlayerProtection());
        register(new JoinListener(this));
        register(new JoinMessage(this));
        register(new ItemProtection(this, buildMode));
        register(new DoubleJump(this));
    }

    public void Commands() {
        getCommand("SolarHub").setExecutor(new dev.eychro.solarHub.Commands.Admin.SolarHub(this));
        getCommand("Discord").setExecutor(new Discord(this));
        getCommand("Website").setExecutor(new Website(this));
        getCommand("Spawn").setExecutor(new Spawn(this));
        getCommand("BuildMode").setExecutor(new BuildMode(this));
        getCommand("SetSpawn").setExecutor(new SetSpawn(this));
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
