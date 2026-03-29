package org.grappepie.mimic.persistence;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.grappepie.mimic.properties.MimicState;

public class MimicPersistentData {

    private static NamespacedKey key(JavaPlugin plugin, String name) {
        return new NamespacedKey(plugin, name);
    }

    public static void tag(Block block, MimicState type, JavaPlugin plugin) {
        if (!(block.getState() instanceof TileState state)) return;
        state.getPersistentDataContainer().set(key(plugin, "type"), PersistentDataType.STRING, type.name());
        state.update();
    }

    public static MimicState getType(Block block, JavaPlugin plugin) {
        if (!(block.getState() instanceof TileState state)) return null;
        String typeName = state.getPersistentDataContainer()
                .get(key(plugin, "type"), PersistentDataType.STRING);
        if (typeName == null) return null;
        try {
            return MimicState.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static void setDouble(Block block, String keyName, double value, JavaPlugin plugin) {
        if (!(block.getState() instanceof TileState state)) return;
        state.getPersistentDataContainer().set(key(plugin, keyName), PersistentDataType.DOUBLE, value);
        state.update();
    }

    public static double getDouble(Block block, String keyName, double defaultVal, JavaPlugin plugin) {
        if (!(block.getState() instanceof TileState state)) return defaultVal;
        Double val = state.getPersistentDataContainer().get(key(plugin, keyName), PersistentDataType.DOUBLE);
        return val != null ? val : defaultVal;
    }

    public static void clear(Block block, JavaPlugin plugin) {
        if (!(block.getState() instanceof TileState state)) return;
        PersistentDataContainer pdc = state.getPersistentDataContainer();
        pdc.remove(key(plugin, "type"));
        pdc.remove(key(plugin, "max_health"));
        pdc.remove(key(plugin, "health"));
        pdc.remove(key(plugin, "scan_radius"));
        state.update();
    }
}
