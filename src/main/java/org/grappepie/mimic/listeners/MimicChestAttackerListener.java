package org.grappepie.mimic.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.grappepie.mimic.properties.MimicChestAttacker;
import org.grappepie.mimic.properties.MimicChestPart;
import org.grappepie.mimic.registry.MimicRegistry;

/**
 * Handles player-hit events directed at attacker mimics.
 * Left-clicking a registered attacker chest deals 1 damage to it.
 */
public class MimicChestAttackerListener implements Listener {

    private final MimicRegistry registry;

    public MimicChestAttackerListener(MimicRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onPlayerHitAttacker(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        MimicChestPart part = registry.get(event.getClickedBlock());
        if (!(part instanceof MimicChestAttacker attacker)) return;

        event.setCancelled(true);
        attacker.onTakeDamage(1.0);
    }
}
