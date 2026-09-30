package dev.eychro.solarHub;

import dev.eychro.solarHub.Commands.Admin.BuildMode;
import dev.eychro.solarHub.Commands.Admin.SetSpawn;
import dev.eychro.solarHub.Commands.Admin.Vanish;
import dev.eychro.solarHub.Commands.User.Discord;
import dev.eychro.solarHub.Commands.User.Fly;
import dev.eychro.solarHub.Commands.User.Spawn;
import dev.eychro.solarHub.Commands.User.Website;
import dev.eychro.solarHub.Features.DoubleJump;
import dev.eychro.solarHub.Features.LaunchPad;
import dev.eychro.solarHub.Features.Welcome;
import dev.eychro.solarHub.Listeners.Player.JoinListener;
import dev.eychro.solarHub.Listeners.Player.JoinMessage;
import dev.eychro.solarHub.Listeners.Player.PlayerProtection;
import dev.eychro.solarHub.Listeners.World.BlockInteractions;
import dev.eychro.solarHub.Listeners.World.ItemProtection;
import dev.eychro.solarHub.Listeners.World.MobsSpawn;
import dev.eychro.solarHub.Managers.FileManager;
import dev.eychro.solarHub.Managers.FlyManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class SolarHub extends JavaPlugin {

    private FileManager files;
    private BuildMode buildMode;
    private Vanish vanish;

    @Override
    public void onEnable() {
        vanish = new Vanish(this);
        buildMode = new BuildMode(this);

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
        registerListener(new ItemProtection(this, buildMode));
        registerListener(new PlayerProtection(this));
        registerListener(new JoinListener(this));
        registerListener(new JoinMessage(this));
        registerListener(new MobsSpawn());
        registerListener(new DoubleJump(this));
        registerListener(new LaunchPad(this));
        registerListener(new Welcome(this));

        registerListener(vanish);
    }

    public void Commands() {
        registerCommand("SolarHub", new dev.eychro.solarHub.Commands.Admin.SolarHub(this));
        registerCommand("Discord", new Discord(this));
        registerCommand("Website", new Website(this));
        registerCommand("Spawn", new Spawn(this));
        registerCommand("BuildMode", buildMode);
        registerCommand("SetSpawn", new SetSpawn(this));
        registerCommand("Fly", new Fly(this));
        registerCommand("Vanish", new Vanish(this));

        registerCommand("Vanish", vanish);
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

    private final FlyManager flyManager = new FlyManager();

    public FlyManager getFlyManager() {
        return flyManager;
    }

    public Vanish getVanish() {
        return vanish;
    }
}