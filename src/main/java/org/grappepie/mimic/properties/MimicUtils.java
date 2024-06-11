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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MimicUtils {

    // Lista de tipos de cofres
    static final List<Material> chestTypes = Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST);

    // NMS Reflection
    private static final String NMS_VERSION = getNMSVersion();
    private static final Class<?> BLOCK_POSITION_CLASS = getNMSClass("net.minecraft.core.BlockPosition");
    private static final Constructor<?> BLOCK_POSITION_CONSTRUCTOR = getConstructor(BLOCK_POSITION_CLASS, int.class, int.class, int.class);
    private static final Class<?> PACKET_PLAY_OUT_BLOCK_ACTION_CLASS = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutBlockAction");
    private static final Class<?> BLOCK_CLASS = getNMSClass("net.minecraft.world.level.block.Block");
    private static final Constructor<?> PACKET_PLAY_OUT_BLOCK_ACTION_CONSTRUCTOR = getConstructor(PACKET_PLAY_OUT_BLOCK_ACTION_CLASS, BLOCK_POSITION_CLASS, BLOCK_CLASS, int.class, int.class);
    private static final Method GET_BY_ID_METHOD = getMethod(BLOCK_CLASS, "a", Material.class);
    private static final Class<?> PACKET_PLAY_OUT_ENTITY_EQUIPMENT_CLASS = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment");
    private static final Class<?> ITEM_STACK_CLASS = getNMSClass("net.minecraft.world.item.ItemStack");
    private static final Class<?> ENUM_ITEM_SLOT_CLASS = getNMSClass("net.minecraft.world.entity.EnumItemSlot");
    private static final Constructor<?> PACKET_PLAY_OUT_ENTITY_EQUIPMENT_CONSTRUCTOR = getConstructor(PACKET_PLAY_OUT_ENTITY_EQUIPMENT_CLASS, int.class, ENUM_ITEM_SLOT_CLASS, ITEM_STACK_CLASS);
    private static final Class<?> PACKET_PLAY_OUT_WORLD_PARTICLES_CLASS = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutWorldParticles");
    private static final Class<?> ENUM_PARTICLE_CLASS = getNMSClass("net.minecraft.core.particles.ParticleType");
    private static final Constructor<?> PACKET_PLAY_OUT_WORLD_PARTICLES_CONSTRUCTOR = getConstructor(PACKET_PLAY_OUT_WORLD_PARTICLES_CLASS, ENUM_PARTICLE_CLASS, boolean.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, int.class, int[].class);

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
        if (open){
            chest.open();
        } else {
            chest.close();
        }
        chest.update(true);
    }

    private static void broadcastPacket(Object packet, Player... excludedPlayers) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (Arrays.asList(excludedPlayers).contains(player)) continue;
            try {
                Object handle = player.getClass().getMethod("getHandle").invoke(player);
                Object playerConnection = handle.getClass().getField("b").get(handle); // 'b' puede ser el campo correcto para 'playerConnection'
                playerConnection.getClass().getMethod("a", getNMSClass("net.minecraft.network.protocol.Packet")).invoke(playerConnection, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
        try {
            Object nmsItemStack = itemStack != null ? getNMSClass("org.bukkit.craftbukkit." + NMS_VERSION + ".inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack) : null;
            Object enumItemSlot = ENUM_ITEM_SLOT_CLASS.getMethod("valueOf", String.class).invoke(null, slot);
            Object packet = PACKET_PLAY_OUT_ENTITY_EQUIPMENT_CONSTRUCTOR.newInstance(player.getEntityId(), enumItemSlot, nmsItemStack);
            broadcastPacket(packet, player);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        try {
            Object enumParticle = ENUM_PARTICLE_CLASS.getMethod("valueOf", String.class).invoke(null, particleName);
            Object packet = PACKET_PLAY_OUT_WORLD_PARTICLES_CONSTRUCTOR.newInstance(enumParticle, true, (float) location.getX(), (float) location.getY(), (float) location.getZ(), (float) dif.getX(), (float) dif.getY(), (float) dif.getZ(), speed, count, new int[0]);
            broadcastPacket(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getNMSVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    private static Class<?> getNMSClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            return clazz.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Object getNMSBlock(Material material) {
        try {
            return GET_BY_ID_METHOD.invoke(null, material);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
