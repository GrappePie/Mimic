package org.grappepie.mimic.properties;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class MimicChestService {
    private static MimicChestService instance;
    private final String mimicChestName = ChatColor.translateAlternateColorCodes('&', "&eMimic");
    private final Map<Block, MimicChestPart> mimicParts = new HashMap<>();
    private boolean debugMode = false;

    public MimicChestService() {
        instance = this;
    }

    public static MimicChestService getInstance() {
        return instance;
    }

    public JavaPlugin getPlugin() {
        return JavaPlugin.getProvidingPlugin(getClass());
    }

    public void updateDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        for (MimicChestPart part : mimicParts.values()) {
            part.updateDebugMode(debugMode);
        }
    }

    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (!(event.getPlayer().getGameMode() == GameMode.ADVENTURE || event.getPlayer().getGameMode() == GameMode.SURVIVAL)) return;
        Block block = event.getClickedBlock();
        if (!MimicUtils.chestTypes.contains(block.getType())) return;
        if (!((Chest) block.getState()).getInventory().getViewers().isEmpty() &&
                ((Chest) block.getState()).getInventory().getViewers().get(0).getOpenInventory().getTitle().startsWith(mimicChestName)) return;
        Action action = event.getAction();
        event.setCancelled(true);

        MimicChestPart part = mimicParts.get(block);
        if (part == null || part.isDestroyed()) {
            part = new MimicChestIdle(this, block);
            mimicParts.put(block, part);
        }

        if (part instanceof MimicChestIdle) {
            if (action == Action.RIGHT_CLICK_BLOCK) {
                // Remove the old hologram
                part.removeHologram();

                MimicChestEater eater = new MimicChestEater(this, block, event.getPlayer(), new Timer(), null);
                mimicParts.put(block, eater);
            }
        } else if (part instanceof MimicChestEater) {
            MimicChestEater eater = (MimicChestEater) part;
            if (eater.isOpen()) eater.closeChest(false);
            else eater.openChest(false);
        }

        event.setCancelled(false);
    }

    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!MimicUtils.chestTypes.contains(block.getType())) return;
        if (!((Chest) block.getState()).getInventory().getViewers().isEmpty() &&
                ((Chest) block.getState()).getInventory().getViewers().get(0).getOpenInventory().getTitle().startsWith(mimicChestName)) return;
        event.setCancelled(true);
        MimicChestPart part = mimicParts.get(block);
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            event.setCancelled(false);
            if (part != null) {
                part.onDestroy(true);
                mimicParts.remove(block);
            }
            return;
        }
        if (part instanceof MimicChestAttacker) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "MimicChestAttacker destroyed");
            part.onTakeDamage(1); // Reduce health by 1
            if (part.getHealth() <= 0) {
                part.onDestroy(true); // Perform death animation
                mimicParts.remove(block);
            }
            return;
        }
        if (part instanceof MimicChestEater) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "MimicChestEater destroyed");
            if (((MimicChestEater) part).hasEatenPlayer()) {
                ((MimicChestEater) part).releasePlayer(); // Vomit the eaten player
                part.onDestroy(true);
                mimicParts.remove(block);
            } else {
                part.onDestroy(true);
                MimicChestAttacker attacker = createNewAttacker(block);
                mimicParts.put(block, attacker);
                if (part.getHealth() != null) {
                    attacker.setHealth(part.getHealth());
                }
            }
            return;
        }
        if (part instanceof MimicChestIdle) {
            part.onDestroy(true);
        }
        MimicChestAttacker attacker = createNewAttacker(block);
        if (attacker != null) {
            mimicParts.put(block, attacker);
            if (part != null && part.getHealth() != null) {
                attacker.setHealth(part.getHealth());
            }
        }
    }

    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> blocks = event.blockList();
        blocks.removeIf(block -> {
            if (!MimicUtils.chestTypes.contains(block.getType())) return false;
            if (!((Chest) block.getState()).getInventory().getViewers().isEmpty() &&
                    ((Chest) block.getState()).getInventory().getViewers().get(0).getOpenInventory().getTitle().startsWith(mimicChestName)) return false;
            MimicChestPart part = mimicParts.get(block);
            if (part == null) {
                mimicParts.put(block, createNewAttacker(block));
                return true;
            }
            if (part instanceof MimicChestAttacker) {
                // Check if the entity that caused the explosion is the Mimic
                if (event.getEntity() == part) {
                    // If it is, don't destroy the block
                    return false;
                }
                part.onTakeDamage(1);
                return true;
            }
            if (part instanceof MimicChestEater) {
                part.onDestroy(true);
                MimicChestAttacker attacker = createNewAttacker(block);
                mimicParts.put(block, attacker);
                if (part.getHealth() != null) {
                    attacker.setHealth(part.getHealth());
                }
                return true;
            }
            return false;
        });
    }

    public void onBlockExplode(BlockExplodeEvent event) {
        List<Block> blocks = event.blockList();
        blocks.removeIf(block -> {
            if (!MimicUtils.chestTypes.contains(block.getType())) return false;
            if (!((Chest) block.getState()).getInventory().getViewers().isEmpty() &&
                    ((Chest) block.getState()).getInventory().getViewers().get(0).getOpenInventory().getTitle().startsWith(mimicChestName)) return false;
            MimicChestPart part = mimicParts.get(block);
            if (part == null) {
                mimicParts.put(block, createNewAttacker(block));
                return true;
            }
            if (part instanceof MimicChestAttacker) {
                part.onTakeDamage(1);
                return true;
            }
            if (part instanceof MimicChestEater) {
                part.onDestroy(true);
                MimicChestAttacker attacker = createNewAttacker(block);
                mimicParts.put(block, attacker);
                if (part.getHealth() != null) {
                    attacker.setHealth(part.getHealth());
                }
                return true;
            }
            return false;
        });
    }

    public ItemStack getMimicItem(Map<String, Object> config) {
        ItemStack mimicItem = new ItemStack(Material.CHEST);
        ItemMeta meta = mimicItem.getItemMeta();
        meta.setDisplayName(mimicChestName);
        if (config != null) {
            meta.setDisplayName(meta.getDisplayName() + " " + new JSONObject(config).toString());
        }
        mimicItem.setItemMeta(meta);
        return mimicItem;
    }

    public Block config(Map<String, Object> config, Block block) {
        if (!(block.getState() instanceof Chest)) return null;
        String name = mimicChestName;
        if (config != null) {
            name += " " + new JSONObject(config).toString();
        }
        if (!(block.getState() instanceof Chest)) return null;
        Chest chest = (Chest) block.getState();
        chest.setCustomName(name);
        return block;
    }

    public MimicChestPart getMimicPart(Block block) {
        return mimicParts.get(block);
    }

    public void destroyMimic(Block block, boolean becauseBroken) {
        MimicChestPart part = mimicParts.get(block);
        if (part != null) {
            part.onDestroy(becauseBroken);
            if (part instanceof MimicChestEater && becauseBroken) {
                mimicParts.put(block, createNewAttacker(block));
                return;
            }
            mimicParts.remove(block);
        }
    }

    public void addMimic(Block block, MimicChestPart mimic) {
        mimicParts.put(block, mimic);
    }

    public MimicChestEater createNewEater(Block block, Player player, Double health) {
        if (mimicParts.containsKey(block)) return null;
        MimicChestEater eater = new MimicChestEater(this, block, player, new Timer(), health);
        eater.updateDebugMode(debugMode);
        mimicParts.put(block, eater);
        return eater;
    }

    public MimicChestAttacker createNewAttacker(Block block) {
        return createNewAttacker(block, getCreatingParams(block));
    }

    public MimicChestAttacker createNewAttacker(Block block, Map<String, Object> params) {
        if (mimicParts.containsKey(block)) return null;
        MimicChestAttacker attacker = new MimicChestAttacker(this, block, params);
        attacker.updateDebugMode(debugMode);
        mimicParts.put(block, attacker);
        return attacker;
    }

    public Map<String, Object> getCreatingParams(Block block) {
        if (!MimicUtils.chestTypes.contains(block.getType())) return new HashMap<>();
        if (!((Chest) block.getState()).getInventory().getViewers().isEmpty() &&
                ((Chest) block.getState()).getInventory().getViewers().get(0).getOpenInventory().getTitle().startsWith(mimicChestName)) {
            return new HashMap<>();
        } else {
            if (!(block.getState() instanceof Chest)) return new HashMap<>();
            Chest chest = (Chest) block.getState();
            String customName = chest.getCustomName();
            if (customName == null || customName.isEmpty()) {
                return new HashMap<>();
            }
            String[] nameParts = customName.split(" ");
            if (nameParts.length == 1) {
                return new HashMap<>();
            } else {
                String jsonString = nameParts[1];
                try {
                    return new JSONObject(jsonString).toMap();
                } catch (Exception e) {
                    System.out.println("Can't parse mimic params [" + block.getX() + ":" + block.getY() + ":" + block.getZ() + "]");
                }
            }
        }
        return new HashMap<>();
    }

    public MimicChestEater getEaterForPlayer(Player player) {
        for (MimicChestPart part : mimicParts.values()) {
            if (part instanceof MimicChestEater) {
                MimicChestEater eater = (MimicChestEater) part;
                if (eater.getEatenPlayer() == player) {
                    return eater;
                }
            }
        }
        return null;
    }

    public void changeToIdle(Block block) {
        if (mimicParts.containsKey(block)) {
            MimicChestPart part = mimicParts.get(block);
            if (part instanceof MimicChestEater) {
                part.removeHologram();
                mimicParts.put(block, new MimicChestIdle(this, block));
            }
        }
    }

    public void changeToAttacker(Block block) {
        if (mimicParts.containsKey(block)) {
            MimicChestPart part = mimicParts.get(block);
            if (part instanceof MimicChestEater) {
                part.removeHologram();
                mimicParts.put(block, createNewAttacker(block));
            }
        }
    }
}
