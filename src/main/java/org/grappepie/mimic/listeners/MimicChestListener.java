package org.grappepie.mimic.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.grappepie.mimic.config.MimicConfig;
import org.grappepie.mimic.properties.MimicChestService;
import org.grappepie.mimic.registry.MimicRegistry;

import java.util.Random;

/**
 * Handles all global mimic events: chunk loading, player interaction,
 * block breaking, and explosions.
 * (Replaces both MimicChestListener and MimicChestIdleListener.)
 */
public class MimicChestListener implements Listener {

    private final MimicChestService service;
    private final MimicRegistry registry;
    private final MimicConfig config;
    private final Random random = new Random();

    public MimicChestListener(MimicChestService service, MimicRegistry registry) {
        this.service = service;
        this.registry = registry;
        this.config = service.getConfig();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) return;
        for (BlockState blockState : event.getChunk().getTileEntities()) {
            Block block = blockState.getBlock();
            if (block.getType() != Material.CHEST) continue;
            if (registry.isRegistered(block)) continue;
            if (!isDungeonChest(block)) continue;
            if (random.nextDouble() < config.getChunkMimicSpawnChance()) {
                service.createNewEater(block, null, null);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        service.onPlayerInteract(event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        service.onBlockBreak(event);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        service.onEntityExplode(event);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        service.onBlockExplode(event);
    }

    /**
     * Returns true if the chest qualifies as a "dungeon chest" for mimic spawning.
     * Criteria: the chest must be below the configured max-y level.
     */
    private boolean isDungeonChest(Block block) {
        return block.getY() < config.getChunkMimicMaxY();
    }
}
