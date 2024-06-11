package org.grappepie.mimic.properties;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
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
        sendPlayerEquipment(player, itemStack, "HEAD");
        sendPlayerEquipment(player, null, "MAINHAND");
        sendPlayerEquipment(player, null, "OFFHAND");
        sendPlayerEquipment(player, null, "FEET");
        sendPlayerEquipment(player, null, "LEGS");
        sendPlayerEquipment(player, null, "CHEST");
    }

    public static void sendRealPlayerEquipment(Player player) {
        sendPlayerEquipment(player, player.getInventory().getHelmet(), "HEAD");
        sendPlayerEquipment(player, player.getInventory().getItemInMainHand(), "MAINHAND");
        sendPlayerEquipment(player, player.getInventory().getItemInOffHand(), "OFFHAND");
        sendPlayerEquipment(player, player.getInventory().getBoots(), "FEET");
        sendPlayerEquipment(player, player.getInventory().getLeggings(), "LEGS");
        sendPlayerEquipment(player, player.getInventory().getChestplate(), "CHEST");
    }

    private static void sendPlayerEquipment(Player player, ItemStack itemStack, String slot) {
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

    public static void playParticle(Location location, String particleName, Vector dif, float speed, int count) {
        // This method needs to be implemented using Bukkit/Spigot's particle API.
    }
}
