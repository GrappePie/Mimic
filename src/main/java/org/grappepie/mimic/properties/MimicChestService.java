package org.grappepie.mimic.properties;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.plugin.java.JavaPlugin;
import org.grappepie.mimic.config.MimicConfig;
import org.grappepie.mimic.persistence.MimicPersistentData;
import org.grappepie.mimic.registry.MimicRegistry;

public class MimicChestService {

    private final JavaPlugin plugin;
    private final MimicConfig config;
    private final MimicRegistry registry;
    private boolean debugMode = false;

    public MimicChestService(JavaPlugin plugin, MimicConfig config, MimicRegistry registry) {
        this.plugin = plugin;
        this.config = config;
        this.registry = registry;
    }

    public JavaPlugin getPlugin() { return plugin; }
    public MimicConfig getConfig() { return config; }
    public MimicRegistry getRegistry() { return registry; }

    // -------------------------------------------------------------------------
    // Debug mode
    // -------------------------------------------------------------------------

    public void updateDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        for (MimicChestPart part : registry.all()) {
            part.updateDebugMode(debugMode);
        }
    }

    // -------------------------------------------------------------------------
    // Event handlers
    // -------------------------------------------------------------------------

    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getPlayer().getGameMode() != GameMode.ADVENTURE
                && event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;

        Block block = event.getClickedBlock();
        if (!MimicUtils.isChest(block)) return;

        Action action = event.getAction();
        MimicChestPart part = registry.get(block);

        // Not a registered mimic — let Bukkit handle the chest normally
        if (part == null || part.isDestroyed()) return;

        if (part instanceof MimicChestIdle) {
            if (action == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                part.removeHologram();
                part.onDestroy(false);
                registry.unregister(block);
                MimicChestEater eater = new MimicChestEater(this, block, event.getPlayer(), null);
                registry.register(block, eater);
                MimicPersistentData.tag(block, MimicState.EATER, plugin);
                eater.updateDebugMode(debugMode);
            }
        } else if (part instanceof MimicChestEater eater) {
            event.setCancelled(true);
            if (eater.isOpen()) eater.closeChest(false);
            else eater.openChest(false);
        } else if (part instanceof MimicChestAttacker) {
            event.setCancelled(true);
        }
    }

    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!MimicUtils.isChest(block)) return;

        MimicChestPart part = registry.get(block);
        if (part == null) return;

        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            part.onDestroy(false);
            registry.unregister(block);
            MimicPersistentData.clear(block, plugin);
            return;
        }

        event.setCancelled(true);

        if (part instanceof MimicChestAttacker attacker) {
            attacker.onTakeDamage(1);
            if (attacker.isDestroyed()) {
                registry.unregister(block);
                MimicPersistentData.clear(block, plugin);
            }
            return;
        }

        if (part instanceof MimicChestEater eater) {
            if (eater.hasEatenPlayer()) {
                eater.releasePlayer();
                eater.onDestroy(true);
                registry.unregister(block);
                MimicPersistentData.clear(block, plugin);
            } else {
                Double savedHealth = eater.getHealth();
                eater.onDestroy(true);
                registry.unregister(block);
                MimicPersistentData.clear(block, plugin);
                MimicChestAttacker newAttacker = createNewAttacker(block);
                if (newAttacker != null && savedHealth != null) {
                    newAttacker.setHealth(savedHealth);
                }
            }
            return;
        }

        if (part instanceof MimicChestIdle) {
            part.onDestroy(true);
            registry.unregister(block);
            MimicPersistentData.clear(block, plugin);
            createNewAttacker(block);
        }
    }

    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> {
            if (!MimicUtils.isChest(block)) return false;
            handleExplosionOnChest(block, event.getEntity() == registry.get(block));
            return true;
        });
    }

    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> {
            if (!MimicUtils.isChest(block)) return false;
            handleExplosionOnChest(block, false);
            return true;
        });
    }

    private void handleExplosionOnChest(Block block, boolean selfExplosion) {
        MimicChestPart part = registry.get(block);
        if (part == null) {
            createNewAttacker(block);
            return;
        }
        if (part instanceof MimicChestAttacker attacker) {
            if (!selfExplosion) {
                attacker.onTakeDamage(1);
                if (attacker.isDestroyed()) {
                    registry.unregister(block);
                    MimicPersistentData.clear(block, plugin);
                }
            }
        } else if (part instanceof MimicChestEater eater) {
            Double savedHealth = eater.getHealth();
            eater.onDestroy(true);
            registry.unregister(block);
            MimicPersistentData.clear(block, plugin);
            MimicChestAttacker newAttacker = createNewAttacker(block);
            if (newAttacker != null && savedHealth != null) {
                newAttacker.setHealth(savedHealth);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------

    public MimicChestIdle createNewIdle(Block block) {
        if (registry.isRegistered(block)) return null;
        MimicChestIdle idle = new MimicChestIdle(this, block);
        idle.updateDebugMode(debugMode);
        registry.register(block, idle);
        MimicPersistentData.tag(block, MimicState.IDLE, plugin);
        return idle;
    }

    public MimicChestEater createNewEater(Block block, Player player, Double health) {
        if (registry.isRegistered(block)) return null;
        MimicChestEater eater = new MimicChestEater(this, block, player, health);
        eater.updateDebugMode(debugMode);
        registry.register(block, eater);
        MimicPersistentData.tag(block, MimicState.EATER, plugin);
        return eater;
    }

    public MimicChestAttacker createNewAttacker(Block block) {
        return createNewAttacker(block,
                config.getAttackerDefaultMaxHealth(),
                null,
                config.getAttackerDefaultScanRadius());
    }

    public MimicChestAttacker createNewAttacker(Block block, double maxHealth, Double health, double scanRadius) {
        if (registry.isRegistered(block)) return null;
        MimicChestAttacker attacker = new MimicChestAttacker(this, block, maxHealth, health, scanRadius);
        attacker.updateDebugMode(debugMode);
        registry.register(block, attacker);
        MimicPersistentData.tag(block, MimicState.ATTACKER, plugin);
        return attacker;
    }

    /** Replaces the current registry entry for a block without checking isRegistered. */
    public void replaceWith(Block block, MimicChestPart newPart) {
        registry.register(block, newPart);
        newPart.updateDebugMode(debugMode);
        MimicPersistentData.tag(block, newPart.getState(), plugin);
    }

    // -------------------------------------------------------------------------
    // State transitions
    // -------------------------------------------------------------------------

    public void changeToIdle(Block block) {
        MimicChestPart part = registry.get(block);
        if (part == null) return;
        part.onDestroy(false);
        registry.unregister(block);
        createNewIdle(block);
    }

    public void changeToAttacker(Block block) {
        MimicChestPart part = registry.get(block);
        if (part == null) return;
        Double savedHealth = part.getHealth();
        part.onDestroy(false);
        registry.unregister(block);
        MimicChestAttacker attacker = createNewAttacker(block);
        if (attacker != null && savedHealth != null) {
            attacker.setHealth(savedHealth);
        }
    }

    // -------------------------------------------------------------------------
    // Misc helpers
    // -------------------------------------------------------------------------

    public MimicChestPart getMimicPart(Block block) {
        return registry.get(block);
    }

    public void destroyMimic(Block block, boolean becauseBroken) {
        MimicChestPart part = registry.get(block);
        if (part == null) return;
        part.onDestroy(becauseBroken);
        registry.unregister(block);
        MimicPersistentData.clear(block, plugin);
    }

    public void addMimic(Block block, MimicChestPart mimic) {
        if (mimic == null) return;
        registry.register(block, mimic);
        MimicPersistentData.tag(block, mimic.getState(), plugin);
    }

    public MimicChestEater getEaterForPlayer(Player player) {
        return registry.getEaterForPlayer(player);
    }

    public void logInfo(String message) {
        plugin.getServer().getConsoleSender().sendMessage(
                Component.text("[Mimic] ", NamedTextColor.AQUA)
                        .append(Component.text(message, NamedTextColor.WHITE)));
    }

    public void logWarning(String message) {
        plugin.getServer().getConsoleSender().sendMessage(
                Component.text("[Mimic] ", NamedTextColor.AQUA)
                        .append(Component.text(message, NamedTextColor.RED)));
    }
}
