package org.grappepie.mimic.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.grappepie.mimic.properties.MimicChestEater;
import org.grappepie.mimic.properties.MimicChestService;

public class MimicChestEaterListener implements Listener {
    private final MimicChestService mimicChestService;

    public MimicChestEaterListener(MimicChestService mimicChestService) {
        this.mimicChestService = mimicChestService;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        MimicChestEater eater = getEater(event.getPlayer());
        if (eater != null) {
            eater.onPlayerMove(event);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        MimicChestEater eater = getEater(event.getPlayer());
        if (eater != null) {
            eater.onPlayerQuit(event);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        MimicChestEater eater = getEater(event.getPlayer());
        if (eater != null) {
            eater.onItemDrop(event);
        }
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            MimicChestEater eater = getEater((Player) event.getDamager());
            if (eater != null) {
                eater.onPlayerAttack(event);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        MimicChestEater eater = getEater(event.getEntity());
        if (eater != null) {
            eater.onPlayerDeath(event);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            MimicChestEater eater = getEater((Player) event.getWhoClicked());
            if (eater != null) {
                eater.onInvClick(event);
            }
        }
    }

    private MimicChestEater getEater(Player player) {
        // Implement logic to get the MimicChestEater associated with the player
        return mimicChestService.getEaterForPlayer(player);
    }
}
