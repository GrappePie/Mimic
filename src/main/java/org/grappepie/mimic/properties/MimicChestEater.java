package org.grappepie.mimic.properties;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class MimicChestEater extends MimicChestPart {
    private final Inventory inventory;
    private final Location teleportLocation;
    private Timer eatingTimer;
    private TimerTask eaterProcess;
    private boolean isOpen = false;
    private double eatItemChance = 0.5;

    private Player eatenPlayer;
    private GameMode eatenPlayerGameMode;
    private boolean eatenPlayerAllowedFly;

    public MimicChestEater(MimicChestService service, Block block, Player awakener, Timer workspace, Double health) {
        super(service, block);
        this.health = health;
        this.eatingTimer = workspace;
        this.inventory = ((Chest) block.getState()).getInventory();
        this.teleportLocation = block.getLocation().add(0.5, -0.9, 0.5);
        this.eatenPlayer = awakener;
        this.state = MimicState.EATER;

        generatePlayerHead(awakener); // Pone la cabeza del jugador en la ranura 0 (de lo contrario, no se cargaría la skin del cráneo)

        openChest(true);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(service.getPlugin(), () -> {
                    closeChest(false);
                    playBurpSound(teleportLocation);
                });
            }
        }, 2 * 50);

        if (eatenPlayer != null) {
            eatenPlayerGameMode = awakener.getGameMode();
            awakener.teleport(teleportLocation);
            awakener.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
            MimicUtils.sendFakePlayerEquipment(awakener, getPlayerHead());
            awakener.setGameMode(GameMode.ADVENTURE);
            eatenPlayerAllowedFly = awakener.getAllowFlight();
            awakener.setAllowFlight(true);
        }

        eaterProcess = new TimerTask() {
            @Override
            public void run() {
                if (eatenPlayer != null) {
                    Bukkit.getScheduler().runTask(service.getPlugin(), () -> {
                        awakener.damage(0.5);
                        if (Math.random() < eatItemChance) {
                            eatPlayerItem();
                        }
                    });
                }
            }
        };
        eatingTimer.schedule(eaterProcess, 0, 20 * 50);

        startListeners();
    }

    private void startListeners() {
        // Implementar listeners de eventos aquí
    }

    private void eatPlayerItem() {
        if (eatenPlayer == null) return;

        Inventory inv = eatenPlayer.getInventory();
        List<Integer> slots = new ArrayList<>();
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) slots.add(i);
        }
        if (eatenPlayer.getInventory().getHelmet() != null) slots.add(-4);
        if (eatenPlayer.getInventory().getChestplate() != null) slots.add(-3);
        if (eatenPlayer.getInventory().getLeggings() != null) slots.add(-2);
        if (eatenPlayer.getInventory().getBoots() != null) slots.add(-1);
        if (slots.isEmpty()) return;
        int slot = slots.get(new Random().nextInt(slots.size()));
        ItemStack itemStack;
        if (slot == -4) {
            itemStack = eatenPlayer.getInventory().getHelmet();
            eatenPlayer.getInventory().setHelmet(null);
        } else if (slot == -3) {
            itemStack = eatenPlayer.getInventory().getChestplate();
            eatenPlayer.getInventory().setChestplate(null);
        } else if (slot == -2) {
            itemStack = eatenPlayer.getInventory().getLeggings();
            eatenPlayer.getInventory().setLeggings(null);
        } else if (slot == -1) {
            itemStack = eatenPlayer.getInventory().getBoots();
            eatenPlayer.getInventory().setBoots(null);
        } else {
            itemStack = contents[slot];
            inv.setItem(slot, null);
        }
        eatItem(itemStack);
    }

    private void processAllergy() {
        List<ItemStack> items = Arrays.asList(inventory.getContents());
        inventory.clear();
        eaterProcess.cancel();
        new Timer().schedule(new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                Bukkit.getScheduler().runTask(service.getPlugin(), () -> {
                    if (i >= items.size()) {
                        MimicChestService.getInstance().destroyMimic(block, false);
                        if (health != null) {
                            MimicChestAttacker attacker = MimicChestService.getInstance().createNewAttacker(block, new HashMap<>());
                            attacker.setHealth(health);
                        }
                        cancel();
                        return;
                    }
                    Entity entity = block.getWorld().dropItem(block.getLocation(), items.get(i));
                    barfEntity(entity);
                    i++;
                });
            }
        }, 0, 2 * 50);
    }

    private void onInvClick(InventoryClickEvent event) {
        if (eatenPlayer != null && event.getWhoClicked() == eatenPlayer) {
            event.setCancelled(true);
        }
    }

    private void onPlayerMove(PlayerMoveEvent event) {
        if (event.getPlayer() != eatenPlayer) return;
        if (event instanceof PlayerTeleportEvent) {
            event.setCancelled(true);
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            event.setTo(from);
        }
    }

    private void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity() != eatenPlayer) return;
        List<ItemStack> items = new ArrayList<>(event.getDrops());
        event.getDrops().clear();
        items.forEach(this::eatItem);
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        barfEntity(block.getWorld().dropItem(block.getLocation(), skull));

        // Comprobar jugadores cercanos
        List<Player> nearbyPlayers = checkNearbyPlayers();
        if (nearbyPlayers.isEmpty()) {
            MimicChestIdle idle = new MimicChestIdle(service, block);
            service.addMimic(block, idle);
        } else {
            Player nextPlayer = nearbyPlayers.get(0);
            eatenPlayer = nextPlayer;
            eatenPlayerGameMode = nextPlayer.getGameMode();
            nextPlayer.teleport(teleportLocation);
            nextPlayer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
            MimicUtils.sendFakePlayerEquipment(nextPlayer, getPlayerHead());
            nextPlayer.setGameMode(GameMode.ADVENTURE);
            eatenPlayerAllowedFly = nextPlayer.getAllowFlight();
            nextPlayer.setAllowFlight(true);
        }
    }

    private List<Player> checkNearbyPlayers() {
        return block.getWorld().getPlayers().stream()
                .filter(player -> player.getLocation().distance(block.getLocation()) <= 5)
                .collect(Collectors.toList());
    }

    private void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer() != eatenPlayer) return;
        Player player = eatenPlayer;
        player.setHealth(0); // Llama a onPlayerDeath
        eatenPlayer = null;
    }

    private void onItemDrop(PlayerDropItemEvent event) {
        if (event.getPlayer() != eatenPlayer) return;
        eatItem(event.getItemDrop().getItemStack());
        event.getItemDrop().remove();
    }

    private void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() != eatenPlayer) return;
        if (!isOpen) event.setCancelled(true);
    }

    private void eatItem(ItemStack itemStack) {
        if (itemStack.getType() == Material.COD || itemStack.getType() == Material.SALMON) {
            processAllergy();
            return;
        }
        if (inventory.firstEmpty() == -1) {
            Entity item = block.getWorld().dropItem(block.getLocation(), itemStack);
            barfEntity(item);
        } else {
            inventory.addItem(itemStack);
            block.getWorld().playSound(block.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
        }
    }

    private void barfEntity(Entity entity) {
        if (entity == null) {
            return;
        }
        entity.teleport(block.getLocation());
        MimicUtils.openChest(block, true);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(service.getPlugin(), () -> {
                    playBurpSound(block.getLocation());
                    entity.setVelocity(getBarfVector().multiply(0.5));
                    MimicUtils.closeChest(block, true);
                });
            }
        }, 5 * 50);
    }

    private Vector getBarfVector() {
        // Use the chest's direction to determine the barf vector
        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            Directional directional = (Directional) chest.getBlockData();
            BlockFace facing = directional.getFacing();
            switch (facing) {
                case NORTH:
                    return new Vector(0, 1, -1);
                case SOUTH:
                    return new Vector(0, 1, 1);
                case WEST:
                    return new Vector(-1, 1, 0);
                case EAST:
                    return new Vector(1, 1, 0);
                default:
                    return new Vector(0, 1, 0);
            }
        }
        return new Vector(0, 1, 0);
    }

    private static void playBurpSound(Location loc) {
        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_BURP, 1, 1);
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void openChest(boolean silent) {
        MimicUtils.openChest(block, silent);
        isOpen = true;
        if (eatenPlayer != null) {
            eatenPlayer.removePotionEffect(PotionEffectType.BLINDNESS);
            MimicUtils.sendFakePlayerEquipment(eatenPlayer, getPlayerHead());
        }
    }

    public void closeChest(boolean silent) {
        MimicUtils.closeChest(block, silent);
        isOpen = false;
        if (eatenPlayer != null) {
            eatenPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0));
            MimicUtils.sendFakePlayerEquipment(eatenPlayer, null);
        }
    }

    private Inventory fakeInventory;

    private void generatePlayerHead(Player player) {
        if (fakeInventory == null) {
            fakeInventory = Bukkit.createInventory(null, 9);
        }
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName("Mimic's head!");
        itemStack.setItemMeta(meta);
        fakeInventory.setItem(0, itemStack);
    }

    private ItemStack getPlayerHead() {
        return fakeInventory.getItem(0);
    }

    @Override
    public void onDestroy(boolean becauseBroken) {
        destroyed = true;
        eatingTimer.cancel();
        if (eatenPlayer != null) {
            barfEntity(eatenPlayer);
            eatenPlayer.setAllowFlight(eatenPlayerAllowedFly);
            eatenPlayer.setGameMode(eatenPlayerGameMode);
            for (PotionEffect effect : eatenPlayer.getActivePotionEffects()) {
                eatenPlayer.removePotionEffect(effect.getType());
            }
            MimicUtils.sendRealPlayerEquipment(eatenPlayer);
            eatenPlayer = null;
        }
        removeHologram(); // Asegúrate de que el holograma se elimine cuando se destruya
    }

    @Override
    public void onTakeDamage(double damage) {
        this.health -= damage;
        if (this.health <= 0) {
            this.onDestroy(true);
        }
    }
}
