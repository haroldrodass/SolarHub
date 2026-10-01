package dev.eychro.solarHub.Managers;

import dev.eychro.solarHub.SolarHub;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ParkourManager {

    public static final String EXIT_PDC = "solar_parkour_exit";
    private static final int EXIT_SLOT = 8;

    private final SolarHub plugin;

    private final Map<UUID, Long> startTimer = new ConcurrentHashMap<>();
    private final Map<UUID, Location> checkpoints = new ConcurrentHashMap<>();
    private final Map<UUID, Location> startLocations = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> checkpointsPassed = new ConcurrentHashMap<>();
    private final Map<UUID, ItemStack[]> savedInventories = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> previousAllowFlight = new ConcurrentHashMap<>();
    private final Map<UUID, Long> exitLock = new ConcurrentHashMap<>();
    private final Map<UUID, Long> warnCooldown = new ConcurrentHashMap<>();

    private final File dataFile;
    private Location startPlate;
    private final List<Location> checkpointPlates = new ArrayList<>();

    private BukkitTask actionBarTask;

    public ParkourManager(SolarHub plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "parkour.yml");
        loadData();
        startActionBarTask();
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private void loadData() {
        startPlate = null;
        checkpointPlates.clear();
        if (!dataFile.exists()) return;

        YamlConfiguration yml = YamlConfiguration.loadConfiguration(dataFile);
        startPlate = yml.getLocation("start");
        List<?> list = yml.getList("checkpoints");
        if (list != null) {
            for (Object o : list) {
                if (o instanceof Location loc) checkpointPlates.add(loc);
            }
        }
    }

    private void saveData() {
        YamlConfiguration yml = new YamlConfiguration();
        yml.set("start", startPlate);
        yml.set("checkpoints", new ArrayList<>(checkpointPlates));
        try {
            plugin.getDataFolder().mkdirs();
            yml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo guardar parkour.yml: " + e.getMessage());
        }
    }

    private static boolean sameBlock(Location a, Location b) {
        if (a == null || b == null) return false;
        return Objects.equals(a.getWorld(), b.getWorld())
                && a.getBlockX() == b.getBlockX()
                && a.getBlockY() == b.getBlockY()
                && a.getBlockZ() == b.getBlockZ();
    }

    public void setStartPlate(Block block) {
        this.startPlate = block.getLocation();
        saveData();
    }

    public boolean hasStartPlate() {
        return startPlate != null;
    }

    public boolean isStartPlate(Block block) {
        return startPlate != null && sameBlock(startPlate, block.getLocation());
    }

    public int addCheckpointPlate(Block block) {
        Location loc = block.getLocation();
        for (Location existing : checkpointPlates) {
            if (sameBlock(existing, loc)) return -1;
        }
        checkpointPlates.add(loc);
        saveData();
        return checkpointPlates.size();
    }

    public void clearCheckpointPlates() {
        checkpointPlates.clear();
        saveData();
    }

    public int getTotalCheckpoints() {
        return checkpointPlates.size();
    }

    private int getCheckpointIndex(Block block) {
        Location loc = block.getLocation();
        for (int i = 0; i < checkpointPlates.size(); i++) {
            if (sameBlock(checkpointPlates.get(i), loc)) return i;
        }
        return -1;
    }

    public void startParkour(Player p, Location locationStart) {
        UUID id = p.getUniqueId();

        GameMode gm = p.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) {
            warn(p, "&cNo puedes iniciar el parkour en modo creativo o espectador.");
            return;
        }

        boolean alreadyPlaying = startTimer.containsKey(id);

        if (!alreadyPlaying) {
            savedInventories.put(id, p.getInventory().getContents());
            previousAllowFlight.put(id, p.getAllowFlight());

            p.getInventory().clear();
            giveExitItem(p);
        }

        disableFlight(p);

        startTimer.put(id, System.currentTimeMillis());
        checkpoints.put(id, locationStart.clone());
        startLocations.put(id, locationStart.clone());
        checkpointsPassed.put(id, 0);

        String msg = plugin.getFiles().getMessages().contains("ParkourStarted")
                ? plugin.getFiles().getMessage("ParkourStarted")
                : plugin.getFiles().getMessage("ParkourStart");

        p.sendMessage(msg);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
    }

    public void StartParkour(Player p, Location locationStart) {
        startParkour(p, locationStart);
    }

    public void handleCheckpoint(Player p, Block block) {
        UUID id = p.getUniqueId();
        if (!startTimer.containsKey(id)) return;

        Location center = block.getLocation().add(0.5, 0.1, 0.5);

        // Sin checkpoints registrados: comportamiento antiguo (cualquier placa pesada)
        if (checkpointPlates.isEmpty()) {
            setCheckpoint(p, center);
            return;
        }

        int index = getCheckpointIndex(block);
        if (index < 0) return;

        int passed = checkpointsPassed.getOrDefault(id, 0);
        if (index < passed) return;

        if (index > passed) {
            warn(p, "&cTe saltaste un checkpoint. Debes pasar por todos en orden.");
            return;
        }

        checkpointsPassed.put(id, passed + 1);
        setCheckpoint(p, center);
    }

    public void setCheckpoint(Player p, Location checkpointPos) {
        if (!startTimer.containsKey(p.getUniqueId())) return;

        Location currentCp = checkpoints.get(p.getUniqueId());
        if (currentCp != null
                && Objects.equals(currentCp.getWorld(), checkpointPos.getWorld())
                && currentCp.getBlockX() == checkpointPos.getBlockX()
                && currentCp.getBlockY() == checkpointPos.getBlockY()
                && currentCp.getBlockZ() == checkpointPos.getBlockZ()) {
            return;
        }

        checkpoints.put(p.getUniqueId(), checkpointPos.clone());
        p.sendMessage(plugin.getFiles().getMessage("ParkourCheckPoint"));
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
    }

    public boolean hasAllCheckpoints(Player p) {
        return checkpointsPassed.getOrDefault(p.getUniqueId(), 0) >= checkpointPlates.size();
    }

    public void finishParkour(Player p) {
        UUID id = p.getUniqueId();
        if (!startTimer.containsKey(id)) return;

        if (!hasAllCheckpoints(p)) {
            int passed = checkpointsPassed.getOrDefault(id, 0);
            warn(p, "&cTe faltan checkpoints por pasar (" + passed + "/" + checkpointPlates.size() + ").");
            return;
        }

        Long startTime = startTimer.get(id);
        long milliseconds = System.currentTimeMillis() - startTime;
        String formattedTime = formatTime(milliseconds);

        clearState(p);

        p.sendMessage(plugin.getFiles().getMessage("ParkourFinish"));
        p.sendMessage(plugin.getFiles().getMessage("ParkourTime", "%time%", formattedTime));

        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        p.sendActionBar(Component.empty());
    }

    public void exitParkour(Player p) {
        UUID id = p.getUniqueId();
        if (!startTimer.containsKey(id)) return;

        Location start = startLocations.get(id);
        cancelParkour(p);

        if (start != null) {
            exitLock.put(id, System.currentTimeMillis() + 1500L);
            p.teleport(start);
        }
        p.sendMessage(color("&eHas salido del parkour."));
    }

    public void cancelParkour(Player p) {
        clearState(p);
        exitLock.remove(p.getUniqueId());
        p.sendActionBar(Component.empty());
    }


    private void clearState(Player p) {
        UUID id = p.getUniqueId();
        boolean wasPlaying = startTimer.remove(id) != null;
        checkpoints.remove(id);
        startLocations.remove(id);
        checkpointsPassed.remove(id);
        warnCooldown.remove(id);

        ItemStack[] saved = savedInventories.remove(id);
        Boolean prevFlight = previousAllowFlight.remove(id);

        if (wasPlaying) {
            if (saved != null) p.getInventory().setContents(saved);
            if (prevFlight != null) p.setAllowFlight(prevFlight);
        }
    }

    private void disableFlight(Player p) {
        if (p.isFlying()) p.setFlying(false);
        p.setAllowFlight(false);
    }

    private void giveExitItem(Player p) {
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        if (meta == null) return;

        meta.setDisplayName(color("&c&lSalir del Parkour"));
        List<String> lore = new ArrayList<>();
        lore.add(color("&7Click derecho para salir"));
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, EXIT_PDC), PersistentDataType.STRING, "exit");
        barrier.setItemMeta(meta);

        p.getInventory().setItem(EXIT_SLOT, barrier);
    }

    public void warn(Player p, String msg) {
        long now = System.currentTimeMillis();
        Long last = warnCooldown.get(p.getUniqueId());
        if (last != null && now - last < 2000L) return;
        warnCooldown.put(p.getUniqueId(), now);
        p.sendMessage(color(msg));
    }

    public boolean isExitLocked(Player p) {
        Long until = exitLock.get(p.getUniqueId());
        if (until == null) return false;
        if (System.currentTimeMillis() > until) {
            exitLock.remove(p.getUniqueId());
            return false;
        }
        return true;
    }

    public void extendExitLock(Player p) {
        exitLock.put(p.getUniqueId(), System.currentTimeMillis() + 1500L);
    }

    public String formatTime(long milli) {
        long seconds = milli / 1000;
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        long remainingMillis = (milli % 1000) / 100;

        return String.format("%02d:%02d.%d", minutes, remainingSeconds, remainingMillis);
    }

    public void startActionBarTask() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
        }

        actionBarTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : startTimer.keySet()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null || !player.isOnline()) continue;

                    // Refuerzo: nada de fly / doble salto mientras está en el parkour
                    if (player.getAllowFlight() || player.isFlying()) {
                        disableFlight(player);
                    }

                    Long start = startTimer.get(uuid);
                    if (start == null) continue;

                    long elapsedMillis = System.currentTimeMillis() - start;
                    String timeStr = formatTime(elapsedMillis);

                    int passed = checkpointsPassed.getOrDefault(uuid, 0);
                    String cp = checkpointPlates.isEmpty()
                            ? ""
                            : " §8| §e§lCheckpoints: §f" + passed + "/" + checkpointPlates.size();

                    player.sendActionBar(Component.text("§e§lTiempo: §f" + timeStr + cp));
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public boolean isPlaying(Player p) {
        return startTimer.containsKey(p.getUniqueId());
    }

    public Location getCheckpoint(Player p) {
        return checkpoints.get(p.getUniqueId());
    }

    public Location getStartLocation(Player p) {
        return startLocations.get(p.getUniqueId());
    }

    public long getStartTime(Player p) {
        return startTimer.getOrDefault(p.getUniqueId(), 0L);
    }

    public void stopTask() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
            actionBarTask = null;
        }

        for (UUID id : new ArrayList<>(startTimer.keySet())) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) cancelParkour(p);
        }
    }
}