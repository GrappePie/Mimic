package org.grappepie.mimic.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.grappepie.mimic.properties.MimicChestService;

public class MimicChestIdleListener implements Listener {
    private final MimicChestService mimicChestService;

    public MimicChestIdleListener(MimicChestService mimicChestService) {
        this.mimicChestService = mimicChestService;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        mimicChestService.onPlayerInteract(event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        mimicChestService.onBlockBreak(event);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        mimicChestService.onEntityExplode(event);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        mimicChestService.onBlockExplode(event);
    }
}
