package org.grappepie.mimic.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.grappepie.mimic.properties.MimicChestService;

import java.util.Random;

public class DungeonChestListener implements Listener {
    private final MimicChestService mimicChestService;
    private final Random random;

    public DungeonChestListener(MimicChestService mimicChestService) {
        this.mimicChestService = mimicChestService;
        this.random = new Random();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        // Iterate over all block states in the chunk and replace some dungeon chests with Mimic chests
        for (BlockState blockState : event.getChunk().getTileEntities()) {
            Block block = blockState.getBlock();
            if (block.getType() == Material.CHEST && isDungeonChest((Chest) blockState)) {
                if (shouldBeMimicChest()) {
                    mimicChestService.createNewEater(block, null, null);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        mimicChestService.onPlayerInteract(event);
    }

    private boolean isDungeonChest(Chest chest) {
        // Implement your logic to determine if this chest is part of a dungeon
        // For example, you might check the surrounding blocks for dungeon features
        return true; // Placeholder logic
    }

    private boolean shouldBeMimicChest() {
        // 20% chance to replace a chest with a Mimic chest
        return random.nextInt(5) == 0;
    }
}
