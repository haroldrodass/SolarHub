package dev.eychro.solarHub.Commands.Admin;

import dev.eychro.solarHub.Managers.ParkourManager;
import dev.eychro.solarHub.SolarHub;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Parkour implements CommandExecutor {

    public static final String PERMISSION = "Solar.Admin.Parkour";

    private final SolarHub plugin;

    public Parkour(SolarHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo jugadores pueden usar este comando.");
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(ParkourManager.color("&cNo tienes permiso para usar este comando."));
            return true;
        }

        ParkourManager manager = plugin.getParkourManager();

        if (command.getName().equalsIgnoreCase("SetParkourStart")) {
            Block plate = findPlate(player, Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
            if (plate == null) {
                player.sendMessage(ParkourManager.color(
                        "&cPárate sobre una placa de presión LIGHT (dorada) para marcarla como inicio."));
                return true;
            }
            manager.setStartPlate(plate);
            player.sendMessage(ParkourManager.color("&aPlaca de inicio del parkour guardada."));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("clear")) {
            manager.clearCheckpointPlates();
            player.sendMessage(ParkourManager.color("&eSe eliminaron todos los checkpoints registrados."));
            return true;
        }

        Block plate = findPlate(player, Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
        if (plate == null) {
            player.sendMessage(ParkourManager.color(
                    "&cPárate sobre una placa de presión HEAVY (de hierro) para registrarla como checkpoint."));
            return true;
        }

        int number = manager.addCheckpointPlate(plate);
        if (number == -1) {
            player.sendMessage(ParkourManager.color("&cEsa placa ya está registrada como checkpoint."));
        } else {
            player.sendMessage(ParkourManager.color("&aCheckpoint #" + number + " registrado."));
        }
        return true;
    }

    private Block findPlate(Player player, Material type) {
        Block feet = player.getLocation().getBlock();
        if (feet.getType() == type) return feet;

        Block below = feet.getRelative(BlockFace.DOWN);
        if (below.getType() == type) return below;

        return null;
    }
}