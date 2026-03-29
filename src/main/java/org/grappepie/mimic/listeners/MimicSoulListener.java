package org.grappepie.mimic.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.grappepie.mimic.items.MimicSoul;
import org.grappepie.mimic.properties.MimicChestService;
import org.grappepie.mimic.properties.MimicUtils;
import org.grappepie.mimic.registry.MimicRegistry;

/**
 * Detects when a Mimic Soul item is placed inside a chest and converts
 * that chest into a Mimic Eater.
 *
 * Trigger: player closes a chest that contains at least one Mimic Soul.
 * The soul is consumed and the chest awakens.
 */
public class MimicSoulListener implements Listener {

    private final JavaPlugin plugin;
    private final MimicChestService service;
    private final MimicRegistry registry;

    public MimicSoulListener(JavaPlugin plugin, MimicChestService service, MimicRegistry registry) {
        this.plugin = plugin;
        this.service = service;
        this.registry = registry;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();

        // Only care about chest inventories
        if (!(inv.getHolder() instanceof Chest chestHolder)) return;

        Block block = chestHolder.getBlock();
        if (!MimicUtils.isChest(block)) return;

        // Already a mimic — ignore
        if (registry.isRegistered(block)) return;

        // Search for a Mimic Soul in the chest contents
        ItemStack soulFound = null;
        for (ItemStack item : inv.getContents()) {
            if (MimicSoul.isMimicSoul(item, plugin)) {
                soulFound = item;
                break;
            }
        }
        if (soulFound == null) return;

        // Consume the soul
        inv.remove(soulFound);

        // Spawn effects at the chest
        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0f, 0.4f);
        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 0.6f, 0.6f);
        block.getWorld().spawnParticle(
                Particle.SOUL,
                block.getLocation().add(0.5, 1.0, 0.5),
                30, 0.4, 0.4, 0.4, 0.03);

        // Convert to Mimic Eater
        service.createNewEater(block, null, null);

        // Notify the player who closed the chest
        if (event.getPlayer() instanceof Player player) {
            player.sendMessage(
                    Component.text("The chest stirs... something inside is ", NamedTextColor.DARK_GRAY)
                            .append(Component.text("hungry.", NamedTextColor.DARK_RED)));
        }
    }
}
