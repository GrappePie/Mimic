package org.grappepie.mimic.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.grappepie.mimic.properties.MimicChestEater;
import org.grappepie.mimic.registry.MimicRegistry;

/**
 * Handles all player-side events that concern an eaten player.
 * Events are forwarded to the relevant MimicChestEater instance.
 * This listener is always registered; it simply no-ops when no eaten player matches.
 */
public class MimicChestEaterListener implements Listener {

    private final MimicRegistry registry;

    public MimicChestEaterListener(MimicRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        MimicChestEater eater = registry.getEaterForPlayer(event.getPlayer());
        if (eater == null) return;

        if (event instanceof PlayerTeleportEvent) {
            // Block all teleports (ender pearl, chorus fruit, etc.) while eaten.
            // Respawn is handled by onPlayerRespawn before this fires.
            event.setCancelled(true);
            return;
        }

        // Prevent any XYZ position change while eaten
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            event.setTo(event.getFrom());
        }
    }

    /**
     * Safety net: if a player respawns while still registered as eaten
     * (e.g. the death event was missed), release them unconditionally.
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        MimicChestEater eater = registry.getEaterForPlayer(event.getPlayer());
        if (eater != null) {
            eater.releasePlayer();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        MimicChestEater eater = registry.getEaterForPlayer(event.getPlayer());
        if (eater == null) return;
        // Kill the player so onPlayerDeath fires and the mimic transitions to idle
        event.getPlayer().setHealth(0);
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        MimicChestEater eater = registry.getEaterForPlayer(event.getPlayer());
        if (eater == null) return;
        // Do NOT cancel — cancelling restores the item to inventory while
        // eatItem() already consumed it, causing duplication.
        eater.eatItem(event.getItemDrop().getItemStack());
        event.getItemDrop().remove();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        MimicChestEater eater = registry.getEaterForPlayer(event.getEntity());
        if (eater == null) return;
        eater.handleEatenPlayerDeath(event);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        MimicChestEater eater = registry.getEaterForPlayer(player);
        if (eater != null) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        MimicChestEater eater = registry.getEaterForPlayer(player);
        if (eater != null && !eater.isOpen()) event.setCancelled(true);
    }
}
