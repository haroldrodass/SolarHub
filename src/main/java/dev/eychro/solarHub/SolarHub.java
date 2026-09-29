package dev.eychro.solarHub;

import dev.eychro.solarHub.Commands.Admin.BuildMode;
import dev.eychro.solarHub.Commands.Admin.SetSpawn;
import dev.eychro.solarHub.Commands.User.Discord;
import dev.eychro.solarHub.Commands.User.Spawn;
import dev.eychro.solarHub.Commands.User.Website;
import dev.eychro.solarHub.Features.DoubleJump;
import dev.eychro.solarHub.Features.LaunchPad;
import dev.eychro.solarHub.Listeners.Player.JoinListener;
import dev.eychro.solarHub.Listeners.Player.JoinMessage;
import dev.eychro.solarHub.Listeners.Player.PlayerProtection;
import dev.eychro.solarHub.Listeners.World.BlockInteractions;
import dev.eychro.solarHub.Listeners.World.ItemProtection;
import dev.eychro.solarHub.Managers.FileManager;
import org.bukkit.command.CommandExecutor;
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

    private void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void registerCommand(String name, CommandExecutor executor) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(executor);
        } else {
            getLogger().severe("¡Error! El comando '" + name + "' no está registrado en el plugin.yml.");
        }
    }

    public void Listeners() {
        registerListener(new BlockInteractions(this, buildMode));
        registerListener(new PlayerProtection());
        registerListener(new JoinListener(this));
        registerListener(new JoinMessage(this));
        registerListener(new ItemProtection(this, buildMode));
        registerListener(new DoubleJump(this));
        registerListener(new LaunchPad(this));
    }

    public void Commands() {
        registerCommand("SolarHub", new dev.eychro.solarHub.Commands.Admin.SolarHub(this));
        registerCommand("Discord", new Discord(this));
        registerCommand("Website", new Website(this));
        registerCommand("Spawn", new Spawn(this));
        registerCommand("BuildMode", new BuildMode(this));
        registerCommand("SetSpawn", new SetSpawn(this));
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
