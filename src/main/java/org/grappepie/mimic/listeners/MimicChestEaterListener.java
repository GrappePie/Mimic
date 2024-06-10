package org.grappepie.mimic.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.grappepie.mimic.properties.MimicChestService;

public class MimicChestEaterListener implements Listener {
    private final MimicChestService mimicChestService;

    public MimicChestEaterListener(MimicChestService mimicChestService) {
        this.mimicChestService = mimicChestService;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        //mimicChestService.onPlayerMove(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        //mimicChestService.onPlayerQuit(event);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        //mimicChestService.onItemDrop(event);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        //mimicChestService.onPlayerAttack(event);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        //mimicChestService.onPlayerDeath(event);
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {
        //mimicChestService.onInvClick(event);
    }
}
