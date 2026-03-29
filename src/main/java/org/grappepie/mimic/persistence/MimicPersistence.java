package org.grappepie.mimic.persistence;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.grappepie.mimic.config.MimicConfig;
import org.grappepie.mimic.properties.*;
import org.grappepie.mimic.registry.MimicRegistry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class MimicPersistence {

    private final JavaPlugin plugin;
    private final MimicRegistry registry;
    private final MimicConfig config;
    private final MimicChestService service;
    private final File saveFile;

    public MimicPersistence(JavaPlugin plugin, MimicRegistry registry,
                            MimicConfig config, MimicChestService service) {
        this.plugin = plugin;
        this.registry = registry;
        this.config = config;
        this.service = service;
        this.saveFile = new File(plugin.getDataFolder(), "mimics.yml");
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        List<Map<String, Object>> entries = new ArrayList<>();

        for (Map.Entry<Block, MimicChestPart> entry : registry.snapshot().entrySet()) {
            Block block = entry.getKey();
            MimicChestPart part = entry.getValue();
            if (part == null || part.isDestroyed()) continue;

            Map<String, Object> data = new HashMap<>();
            data.put("world", block.getWorld().getName());
            data.put("x", block.getX());
            data.put("y", block.getY());
            data.put("z", block.getZ());
            data.put("type", part.getState() != null ? part.getState().name() : MimicState.ATTACKER.name());
            if (part.getHealth() != null) data.put("health", part.getHealth());

            if (part instanceof MimicChestAttacker attacker) {
                data.put("maxHealth", attacker.getMaxHealth());
                data.put("scanRadius", attacker.getScanRadius());
            }
            entries.add(data);
        }

        yaml.set("mimics", entries);
        try {
            yaml.save(saveFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save mimics.yml", e);
        }
    }

    /**
     * Loads saved mimics. Must be called after the server tick starts
     * so chunk/world data is available.
     */
    public void load() {
        if (!saveFile.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(saveFile);
        List<?> entries = yaml.getList("mimics");
        if (entries == null) return;

        // Defer to first server tick so worlds are ready
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Object obj : entries) {
                if (!(obj instanceof Map<?, ?> raw)) continue;
                try {
                    String worldName = (String) raw.get("world");
                    int x = ((Number) raw.get("x")).intValue();
                    int y = ((Number) raw.get("y")).intValue();
                    int z = ((Number) raw.get("z")).intValue();
                    String typeName = (String) raw.get("type");

                    World world = Bukkit.getWorld(worldName);
                    if (world == null) continue;

                    Block block = world.getBlockAt(x, y, z);
                    if (!MimicUtils.isChest(block)) continue;
                    if (registry.isRegistered(block)) continue;

                    MimicState type;
                    try {
                        type = MimicState.valueOf(typeName);
                    } catch (IllegalArgumentException e) {
                        type = MimicState.ATTACKER;
                    }

                    Double health = raw.containsKey("health")
                            ? ((Number) raw.get("health")).doubleValue() : null;

                    switch (type) {
                        case IDLE -> service.createNewIdle(block);
                        case EATER -> service.createNewEater(block, null, health);
                        case ATTACKER -> {
                            double maxHealth = raw.containsKey("maxHealth")
                                    ? ((Number) raw.get("maxHealth")).doubleValue()
                                    : config.getAttackerDefaultMaxHealth();
                            double scanRadius = raw.containsKey("scanRadius")
                                    ? ((Number) raw.get("scanRadius")).doubleValue()
                                    : config.getAttackerDefaultScanRadius();
                            MimicChestAttacker attacker = service.createNewAttacker(
                                    block, maxHealth, health, scanRadius);
                            if (attacker == null) {
                                plugin.getLogger().warning(
                                        "Could not restore Mimic Attacker at " + x + "," + y + "," + z);
                            }
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load a mimic entry", e);
                }
            }
        });
    }
}
