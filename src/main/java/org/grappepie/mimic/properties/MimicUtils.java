package org.grappepie.mimic.properties;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MimicUtils {

    // Lista de tipos de cofres
    static final List<Material> chestTypes = Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST);

    public static void openChest(Block block, boolean silent) {
        if (!chestTypes.contains(block.getType())) return;
        Location loc = block.getLocation();
        World world = loc.getWorld();
        if (world != null && !silent) {
            world.playSound(loc, Sound.BLOCK_CHEST_OPEN, 1, 1);
        }
        playChestAnimation(block, true);
    }

    public static void closeChest(Block block, boolean silent) {
        if (!chestTypes.contains(block.getType())) return;
        Location loc = block.getLocation();
        World world = loc.getWorld();
        if (world != null && !silent) {
            world.playSound(loc, Sound.BLOCK_CHEST_CLOSE, 1, 1);
        }
        playChestAnimation(block, false);
    }

    private static void playChestAnimation(Block block, boolean open) {
        if (!(block.getState() instanceof Chest)) return;
        Chest chest = (Chest) block.getState();
        if (open) {
            chest.open();
        } else {
            chest.close();
        }
        chest.update(true);
    }

    public static void sendFakePlayerEquipment(Player player, ItemStack itemStack) {
        sendPlayerEquipment(player, itemStack, EquipmentSlot.HEAD);
        sendPlayerEquipment(player, null, EquipmentSlot.HAND);
        sendPlayerEquipment(player, null, EquipmentSlot.OFF_HAND);
        sendPlayerEquipment(player, null, EquipmentSlot.FEET);
        sendPlayerEquipment(player, null, EquipmentSlot.LEGS);
        sendPlayerEquipment(player, null, EquipmentSlot.CHEST);
    }

    public static void sendRealPlayerEquipment(Player player) {
        sendPlayerEquipment(player, player.getInventory().getHelmet(), EquipmentSlot.HEAD);
        sendPlayerEquipment(player, player.getInventory().getItemInMainHand(), EquipmentSlot.HAND);
        sendPlayerEquipment(player, player.getInventory().getItemInOffHand(), EquipmentSlot.OFF_HAND);
        sendPlayerEquipment(player, player.getInventory().getBoots(), EquipmentSlot.FEET);
        sendPlayerEquipment(player, player.getInventory().getLeggings(), EquipmentSlot.LEGS);
        sendPlayerEquipment(player, player.getInventory().getChestplate(), EquipmentSlot.CHEST);
    }

    private static void sendPlayerEquipment(Player player, ItemStack itemStack, EquipmentSlot slot) {
        // Bukkit/Spigot does not have a direct method to send equipment packets without NMS.
        // This method could be implemented using ProtocolLib or other packet handling library.
    }

    public static ArrayList<Location> getCircle(Location center, double radius, int amount) {
        World world = center.getWorld();
        double increment = (2 * Math.PI) / amount;
        ArrayList<Location> locations = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            double angle = i * increment;
            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));
            locations.add(new Location(world, x, center.getY(), z));
        }
        return locations;
    }

    public static class MagicCircle extends BukkitRunnable {
        private final Block block;
        private final Color color;
        private double angle = 0;

        public MagicCircle(Block block, Color color) {
            this.block = block;
            this.color = color;
        }

        @Override
        public void run() {
            if (block == null || block.getType() != Material.CHEST) {
                this.cancel();
                return;
            }

            Location center = block.getLocation().add(0.5, -0.5, 0.5);

            // First layer (blue triangle, clockwise)
            double radius1 = 1.5;
            Vector[] triangleVertices = new Vector[3];
            for (int i = 0; i < 3; i++) {
                double theta = angle + i * (2 * Math.PI / 3);
                triangleVertices[i] = new Vector(Math.cos(theta), 0, Math.sin(theta)).multiply(radius1);
            }
            generateParticlesAlongEdges(center, triangleVertices, color);

            // Second layer (cyan square, counter-clockwise)
            double radius2 = 2.0;
            Vector[] squareVertices = new Vector[4];
            for (int i = 0; i < 4; i++) {
                double theta = -angle + i * (2 * Math.PI / 4);
                squareVertices[i] = new Vector(Math.cos(theta), 0, Math.sin(theta)).multiply(radius2);
            }
            generateParticlesAlongEdges(center, squareVertices, color);

            // Third layer (green pentagon, clockwise)
            double radius3 = 2.5;
            Vector[] pentagonVertices = new Vector[5];
            for (int i = 0; i < 5; i++) {
                double theta = angle + i * (2 * Math.PI / 5);
                pentagonVertices[i] = new Vector(Math.cos(theta), 0, Math.sin(theta)).multiply(radius3);
            }
            generateParticlesAlongEdges(center, pentagonVertices, color);

            // Fourth layer (red hexagon, counter-clockwise)
            double radius4 = 3.0;
            Vector[] hexagonVertices = new Vector[6];
            for (int i = 0; i < 6; i++) {
                double theta = -angle + i * (2 * Math.PI / 6);
                hexagonVertices[i] = new Vector(Math.cos(theta), 0, Math.sin(theta)).multiply(radius4);
            }
            generateParticlesAlongEdges(center, hexagonVertices, color);

            // Update the angle for rotation
            angle += Math.PI / 60;
        }

        private void generateParticlesAlongEdges(Location center, Vector[] vertices, Color color) {
            int pointsPerEdge = 20; // Number of points (particles) per edge
            for (int i = 0; i < vertices.length; i++) {
                Vector start = vertices[i];
                Vector end = vertices[(i + 1) % vertices.length];
                for (int j = 0; j <= pointsPerEdge; j++) {
                    Vector point = start.clone().add(end.clone().subtract(start).multiply(j / (double) pointsPerEdge));
                    Location particleLocation = center.clone().add(point);
                    center.getWorld().spawnParticle(Particle.DUST, particleLocation, 1, new Particle.DustOptions(color, 0.5f));
                }
            }
        }
    }
}
