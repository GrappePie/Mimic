package org.grappepie.mimic.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.grappepie.mimic.properties.MimicChestService;

public class MimicChestAttackerListener implements Listener {
    private final MimicChestService mimicChestService;

    public MimicChestAttackerListener(MimicChestService mimicChestService) {
        this.mimicChestService = mimicChestService;
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        //mimicChestService.onPlayerAttack(event);
    }
}
