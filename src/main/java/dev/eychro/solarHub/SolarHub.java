package dev.eychro.solarHub;

import dev.eychro.solarHub.Commands.Admin.BuildMode;
import dev.eychro.solarHub.Commands.Admin.Parkour;
import dev.eychro.solarHub.Commands.Admin.SetSpawn;
import dev.eychro.solarHub.Commands.Admin.Vanish;
import dev.eychro.solarHub.Commands.User.Discord;
import dev.eychro.solarHub.Commands.User.Fly;
import dev.eychro.solarHub.Commands.User.Spawn;
import dev.eychro.solarHub.Commands.User.Website;
import dev.eychro.solarHub.Features.Announcements;
import dev.eychro.solarHub.Features.DoubleJump;
import dev.eychro.solarHub.Features.LaunchPad;
import dev.eychro.solarHub.Features.Welcome;
import dev.eychro.solarHub.Listeners.Player.*;
import dev.eychro.solarHub.Listeners.World.BlockInteractions;
import dev.eychro.solarHub.Listeners.World.ItemProtection;
import dev.eychro.solarHub.Listeners.World.MobsSpawn;
import dev.eychro.solarHub.Commands.User.MenuCommands;
import dev.eychro.solarHub.Listeners.Menu.MenuListener;
import dev.eychro.solarHub.Managers.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class SolarHub extends JavaPlugin {

    private FileManager files;
    private CooldownManager cooldownManager;
    private VisibilityManager visibilityManager;
    private CosmeticsManager cosmeticsManager;
    private MenuManager menuManager;
    private HotbarManager hotbarManager;
    private BuildMode buildMode;
    private Vanish vanish;
    private Announcements announcements;
    private ParkourManager parkourManager;

    @Override
    public void onEnable() {
        Files();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        cooldownManager = new CooldownManager(this);
        visibilityManager = new VisibilityManager(this);
        cosmeticsManager = new CosmeticsManager(this);
        menuManager = new MenuManager(this);
        hotbarManager = new HotbarManager(this);
        vanish = new Vanish(this);
        buildMode = new BuildMode(this);
        parkourManager = new ParkourManager(this);

        announcements = new Announcements(this);
        announcements.Announce();

        Commands();
        Listeners();
    }

    @Override
    public void onDisable() {
        if (announcements != null) announcements.stop();
        if (cosmeticsManager != null) cosmeticsManager.stopTask();
        if (parkourManager != null) parkourManager.stopTask();
        getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
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
        registerListener(new PlayerQuitListener(this, buildMode));
        registerListener(cooldownManager);
        registerListener(visibilityManager);
        registerListener(cosmeticsManager);
        registerListener(hotbarManager);
        registerListener(new MenuListener(this));
        registerListener(new ParkourListener(this, parkourManager));

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
        registerCommand("Vanish", vanish);
        Parkour parkourAdmin = new Parkour(this);
        registerCommand("SetParkourStart", parkourAdmin);
        registerCommand("SetParkourCheckpoint", parkourAdmin);
        registerCommand("servers", new MenuCommands(this, "servers", "Solar.Menu.Servers"));
        registerCommand("cosmetics", new MenuCommands(this, "cosmetics", "Solar.Menu.Cosmetics"));
        registerCommand("profile", new MenuCommands(this, "profile", "Solar.Menu.Profile"));
    }

    public void reloadAll() {
        files.reloadAll();
        announcements.Announce();
    }

    public void Files() {
        files = new FileManager(this);
    }

    public FileManager getFiles() {
        return files;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public VisibilityManager getVisibilityManager() {
        return visibilityManager;
    }

    public CosmeticsManager getCosmeticsManager() {
        return cosmeticsManager;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public HotbarManager getHotbarManager() {
        return hotbarManager;
    }

    public BuildMode getBuildMode() {
        return buildMode;
    }

    private final FlyManager flyManager = new FlyManager();

    public FlyManager getFlyManager() {
        return flyManager;
    }

    public ParkourManager getParkourManager() {
        return parkourManager;
    }

    public Vanish getVanish() {
        return vanish;
    }
}