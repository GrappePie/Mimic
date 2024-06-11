package org.grappepie.mimic.properties;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static void playParticle(Location location, Particle particle, Vector dif, float speed, int count) {
        World world = location.getWorld();
        if (world != null) {
            world.spawnParticle(particle, location, count, dif.getX(), dif.getY(), dif.getZ(), speed);
        }
    }
}
