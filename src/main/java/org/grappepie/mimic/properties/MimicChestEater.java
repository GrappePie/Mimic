package org.grappepie.mimic.properties;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class MimicChestEater extends MimicChestPart {

    private final Inventory inventory;
    private final Location teleportLocation;
    private BukkitTask eaterTask;
    private BukkitTask allergyTask;
    private boolean isOpen = false;
    private final double eatItemChance;

    private Player eatenPlayer;
    private boolean eatenPlayerAllowedFly;
    private MimicUtils.MagicCircle magicCircle;
    private Inventory fakeInventory;

    public MimicChestEater(MimicChestService service, Block block, Player awakener, Double health) {
        super(service, block);
        this.state = MimicState.EATER;
        this.health = health;
        this.eatItemChance = config.getEaterDefaultEatItemChance();
        this.magicCircle = new MimicUtils.MagicCircle(block, Color.ORANGE, config);
        this.magicCircle.runTaskTimer(service.getPlugin(), 0, config.getMagicCircleIntervalTicks());
        this.inventory = ((Chest) block.getState()).getInventory();
        this.teleportLocation = block.getLocation().add(0.5, -0.9, 0.5);
        this.eatenPlayer = awakener;

        if (awakener != null) {
            generatePlayerHead(awakener);
        }

        openChest(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                closeChest(false);
                playBurpSound(teleportLocation);
            }
        }.runTaskLater(service.getPlugin(), 2L);

        if (eatenPlayer != null) {
            awakener.teleport(teleportLocation);
            awakener.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
            MimicUtils.sendFakePlayerEquipment(awakener, getPlayerHead());
            eatenPlayerAllowedFly = awakener.getAllowFlight();
            awakener.setAllowFlight(true);
            startEaterTask();
        }
    }

    private void startEaterTask() {
        if (eaterTask != null) eaterTask.cancel();
        long period = config.getEaterDamagePeriodTicks();
        eaterTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (eatenPlayer == null || destroyed) {
                    cancel();
                    return;
                }
                eatenPlayer.damage(1);
                if (Math.random() < eatItemChance) {
                    eatPlayerItem();
                }
            }
        }.runTaskTimer(service.getPlugin(), period, period);
    }

    public Player getEatenPlayer() {
        return eatenPlayer;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public boolean hasEatenPlayer() {
        return eatenPlayer != null;
    }

    /** Called by MimicChestEaterListener on player death. */
    public void handleEatenPlayerDeath(PlayerDeathEvent event) {
        List<ItemStack> items = new ArrayList<>(event.getDrops());
        event.getDrops().clear();
        items.forEach(this::eatItem);

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(eatenPlayer);
        skull.setItemMeta(meta);
        barfEntity(block.getWorld().dropItem(block.getLocation(), skull));

        releasePlayer();

        Bukkit.getScheduler().runTask(service.getPlugin(), () -> {
            if (destroyed) return;
            List<Player> nearbyPlayers = checkNearbyPlayers();
            if (nearbyPlayers.isEmpty()) {
                service.changeToIdle(block);
            } else {
                eatPlayer(nearbyPlayers.get(0));
            }
        });
    }

    public void releasePlayer() {
        if (eatenPlayer == null) return;
        if (eaterTask != null) { eaterTask.cancel(); eaterTask = null; }
        eatenPlayer.setAllowFlight(eatenPlayerAllowedFly);
        for (PotionEffect effect : eatenPlayer.getActivePotionEffects()) {
            eatenPlayer.removePotionEffect(effect.getType());
        }
        MimicUtils.sendRealPlayerEquipment(eatenPlayer);
        eatenPlayer = null;
    }

    public void clearMagicCircle() {
        if (magicCircle != null) {
            magicCircle.cancel();
            magicCircle = null;
        }
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

    public void eatPlayer(Player player) {
        this.eatenPlayer = player;
        generatePlayerHead(player);
        player.teleport(teleportLocation);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
        MimicUtils.sendFakePlayerEquipment(player, getPlayerHead());
        this.eatenPlayerAllowedFly = player.getAllowFlight();
        player.setAllowFlight(true);
        startEaterTask();
    }

    public void eatItem(ItemStack itemStack) {
        if (itemStack == null) return;
        if (itemStack.getType() == Material.COD || itemStack.getType() == Material.SALMON) {
            processAllergy();
            return;
        }
        if (inventory.firstEmpty() == -1) {
            barfEntity(block.getWorld().dropItem(block.getLocation(), itemStack));
        } else {
            inventory.addItem(itemStack);
            block.getWorld().playSound(block.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
        }
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
        if (eaterTask != null) { eaterTask.cancel(); eaterTask = null; }

        allergyTask = new BukkitRunnable() {
            private int i = 0;

            @Override
            public void run() {
                if (i >= items.size()) {
                    cancel();
                    double savedHealth = health != null ? health : config.getAttackerDefaultMaxHealth();
                    service.destroyMimic(block, false);
                    MimicChestAttacker attacker = service.createNewAttacker(block);
                    if (attacker != null) attacker.setHealth(savedHealth);
                    return;
                }
                ItemStack item = items.get(i);
                if (item != null) {
                    Entity entity = block.getWorld().dropItem(block.getLocation(), item);
                    barfEntity(entity);
                }
                i++;
            }
        }.runTaskTimer(service.getPlugin(), 0L, 2L);
    }

    private void barfEntity(Entity entity) {
        if (entity == null) return;
        entity.teleport(block.getLocation());
        MimicUtils.openChest(block, true);
        new BukkitRunnable() {
            @Override
            public void run() {
                playBurpSound(block.getLocation());
                entity.setVelocity(getBarfVector().multiply(0.5));
                MimicUtils.closeChest(block, true);
            }
        }.runTaskLater(service.getPlugin(), 5L);
    }

    private List<Player> checkNearbyPlayers() {
        return block.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distance(block.getLocation()) <= 5)
                .collect(Collectors.toList());
    }

    private Vector getBarfVector() {
        if (block.getState() instanceof Chest chest) {
            Directional directional = (Directional) chest.getBlockData();
            return switch (directional.getFacing()) {
                case NORTH -> new Vector(0, 1, -1);
                case SOUTH -> new Vector(0, 1, 1);
                case WEST -> new Vector(-1, 1, 0);
                case EAST -> new Vector(1, 1, 0);
                default -> new Vector(0, 1, 0);
            };
        }
        return new Vector(0, 1, 0);
    }

    private static void playBurpSound(Location loc) {
        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_BURP, 1, 1);
    }

    private void generatePlayerHead(Player player) {
        if (fakeInventory == null) {
            fakeInventory = Bukkit.createInventory(null, 9);
        }
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        if (player != null) meta.setOwningPlayer(player);
        meta.setDisplayName("Mimic's head!");
        itemStack.setItemMeta(meta);
        fakeInventory.setItem(0, itemStack);
    }

    private ItemStack getPlayerHead() {
        if (fakeInventory == null) return null;
        return fakeInventory.getItem(0);
    }

    @Override
    public void onDestroy(boolean becauseBroken) {
        if (destroyed) return;
        destroyed = true;
        if (eaterTask != null) { eaterTask.cancel(); eaterTask = null; }
        if (allergyTask != null) { allergyTask.cancel(); allergyTask = null; }
        clearMagicCircle();
        if (eatenPlayer != null) {
            if (becauseBroken) barfEntity(eatenPlayer);
            eatenPlayer.setAllowFlight(eatenPlayerAllowedFly);
            for (PotionEffect effect : eatenPlayer.getActivePotionEffects()) {
                eatenPlayer.removePotionEffect(effect.getType());
            }
            MimicUtils.sendRealPlayerEquipment(eatenPlayer);
            eatenPlayer = null;
        }
        removeHologram();
        removeReachArea();
    }

    @Override
    public void onTakeDamage(double damage) {
        if (health == null) health = config.getAttackerDefaultMaxHealth();
        this.health -= damage;
        if (this.health <= 0) {
            this.onDestroy(true);
        }
    }

    @Override
    protected void showReachArea() {
        // Eater mimics have no ranged attack zone to visualise
    }

    @Override
    protected void removeReachArea() {
        // Eater mimics have no ranged attack zone to visualise
    }
}
