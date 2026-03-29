package org.grappepie.mimic.properties;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.grappepie.mimic.config.MimicConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MimicUtils {

    static final List<Material> chestTypes = Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST);

    public static boolean isChest(Block block) {
        return chestTypes.contains(block.getType());
    }

    public static void openChest(Block block, boolean silent) {
        if (!isChest(block)) return;
        Location loc = block.getLocation();
        World world = loc.getWorld();
        if (world != null && !silent) {
            world.playSound(loc, Sound.BLOCK_CHEST_OPEN, 1, 1);
        }
        playChestAnimation(block, true);
    }

    public static void closeChest(Block block, boolean silent) {
        if (!isChest(block)) return;
        Location loc = block.getLocation();
        World world = loc.getWorld();
        if (world != null && !silent) {
            world.playSound(loc, Sound.BLOCK_CHEST_CLOSE, 1, 1);
        }
        playChestAnimation(block, false);
    }

    private static void playChestAnimation(Block block, boolean open) {
        if (!(block.getState() instanceof Chest chest)) return;
        if (open) {
            chest.open();
        } else {
            chest.close();
        }
        chest.update(true);
    }

    /**
     * Sends fake equipment to a player's model (requires ProtocolLib or Paper packet API).
     * Currently a no-op — implement with ProtocolLib for full visual effect.
     */
    public static void sendFakePlayerEquipment(Player player, ItemStack itemStack) {
        // No-op: packet-level equipment spoofing requires ProtocolLib
    }

    /**
     * Restores a player's real equipment visually.
     * Currently a no-op — implement with ProtocolLib for full visual effect.
     */
    public static void sendRealPlayerEquipment(Player player) {
        // No-op: packet-level equipment spoofing requires ProtocolLib
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
        private final int pointsPerEdge;
        private final double radius1;
        private final double radius2;
        private final double radius3;
        private final double radius4;
        private double angle = 0;

        public MagicCircle(Block block, Color color, MimicConfig config) {
            this.block = block;
            this.color = color;
            this.pointsPerEdge = config.getMagicCirclePointsPerEdge();
            this.radius1 = config.getMagicCircleRadius1();
            this.radius2 = config.getMagicCircleRadius2();
            this.radius3 = config.getMagicCircleRadius3();
            this.radius4 = config.getMagicCircleRadius4();
        }

        @Override
        public void run() {
            if (block == null || !isChest(block)) {
                this.cancel();
                return;
            }

            Location center = block.getLocation().add(0.5, -0.5, 0.5);

            // Triangle (clockwise)
            Vector[] triangleVertices = new Vector[3];
            for (int i = 0; i < 3; i++) {
                double theta = angle + i * (2 * Math.PI / 3);
                triangleVertices[i] = new Vector(Math.cos(theta), 0, Math.sin(theta)).multiply(radius1);
            }
            generateParticlesAlongEdges(center, triangleVertices);

            // Square (counter-clockwise)
            Vector[] squareVertices = new Vector[4];
            for (int i = 0; i < 4; i++) {
                double theta = -angle + i * (2 * Math.PI / 4);
                squareVertices[i] = new Vector(Math.cos(theta), 0, Math.sin(theta)).multiply(radius2);
            }
            generateParticlesAlongEdges(center, squareVertices);

            // Pentagon (clockwise)
            Vector[] pentagonVertices = new Vector[5];
            for (int i = 0; i < 5; i++) {
                double theta = angle + i * (2 * Math.PI / 5);
                pentagonVertices[i] = new Vector(Math.cos(theta), 0, Math.sin(theta)).multiply(radius3);
            }
            generateParticlesAlongEdges(center, pentagonVertices);

            // Hexagon (counter-clockwise)
            Vector[] hexagonVertices = new Vector[6];
            for (int i = 0; i < 6; i++) {
                double theta = -angle + i * (2 * Math.PI / 6);
                hexagonVertices[i] = new Vector(Math.cos(theta), 0, Math.sin(theta)).multiply(radius4);
            }
            generateParticlesAlongEdges(center, hexagonVertices);

            angle += Math.PI / 60;
        }

        private void generateParticlesAlongEdges(Location center, Vector[] vertices) {
            for (int i = 0; i < vertices.length; i++) {
                Vector start = vertices[i];
                Vector end = vertices[(i + 1) % vertices.length];
                for (int j = 0; j <= pointsPerEdge; j++) {
                    Vector point = start.clone().add(end.clone().subtract(start).multiply(j / (double) pointsPerEdge));
                    Location particleLocation = center.clone().add(point);
                    center.getWorld().spawnParticle(Particle.DUST, particleLocation, 1,
                            new Particle.DustOptions(color, 0.5f));
                }
            }
        }
    }
}
