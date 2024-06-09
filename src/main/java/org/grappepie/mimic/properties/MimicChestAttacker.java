package org.grappepie.mimic.properties;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class MimicChestAttacker extends MimicChestPart {
    private final double maxHealth;
    private Timer attackTimer;
    private final double scanRadius;
    private final Location attackLocation;
    private final boolean displayAttackZone;
    private final int maxAttackDelay;
    private int attackDelay;

    public MimicChestAttacker(MimicChestService service, Block block, Map<String, Object> params) {
        super(service, block);

        this.maxHealth = params.get("maxHealth") != null ? (Double) params.get("maxHealth") : 20.0;
        this.health = params.get("health") != null ? (Double) params.get("health") : maxHealth;
        this.maxAttackDelay = params.get("maxAttackDelay") != null ? (Integer) params.get("maxAttackDelay") : 40;
        this.scanRadius = params.get("scanRadius") != null ? (Double) params.get("scanRadius") : 12.0;
        this.displayAttackZone = params.get("displayAttackZone") != null ? (Boolean) params.get("displayAttackZone") : false;

        this.attackDelay = maxAttackDelay;
        this.attackLocation = block.getLocation().add(0.5, 1.2, 0.5);
        this.mount.getWorld().playSound(mount, Sound.ENTITY_GHAST_SCREAM, 2, 1);

        this.state = MimicState.ATTACKER; // Agregar esta línea

        if (displayAttackZone) {
            displayAttackZone();
        }

        this.attackTimer = new Timer();
        attackTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                attack();
            }
        }, 20, 20);

        updateHologram("Attacker"); // Agregar esta línea
    }

    private void displayAttackZone() {
        // Implementar lógica para mostrar la zona de ataque
    }

    private void attack() {
        // Implementar lógica de ataque
    }

    public void openChest(boolean silent) {
        MimicUtils.openChest(block, silent);
    }

    public void closeChest(boolean silent) {
        MimicUtils.closeChest(block, silent);
    }

    private void playDeathEffect(List<ItemStack> items) {
        Firework firework = mount.getWorld().spawn(mount, Firework.class);
        FireworkMeta fwMeta = firework.getFireworkMeta();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            FireworkEffect.Builder effectBuilder = FireworkEffect.builder();
            effectBuilder.flicker(random.nextBoolean());
            effectBuilder.trail(random.nextBoolean());
            effectBuilder.withColor(Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            fwMeta.addEffect(effectBuilder.build());
        }
        fwMeta.setPower(2);
        firework.setFireworkMeta(fwMeta);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                firework.detonate();
                Location loc = firework.getLocation();
                for (ItemStack item : items) {
                    loc.getWorld().dropItem(loc, item).setVelocity(Vector.getRandom().multiply(0.3));
                }
            }
        }, 20);
    }

    @Override
    public void onDestroy(boolean becauseDestroyed) {
        destroyed = true;
        if (becauseDestroyed) {
            mount.getWorld().playSound(mount, Sound.ENTITY_ZOMBIE_HORSE_DEATH, 1, 1);
            List<ItemStack> items = Arrays.asList(chest.getInventory().getContents());
            chest.getInventory().clear();
            items.remove(0);
            items = items.stream().filter(Objects::nonNull).collect(Collectors.toList());
            block.setType(Material.AIR);
            mount.getWorld().createExplosion(mount, 2);
            playDeathEffect(items);
        }
        if (attackTimer != null) {
            attackTimer.cancel();
        }
        removeHologram(); // Agregar esta línea
    }

    @Override
    public void onTakeDamage(double damage) {
        this.health -= damage;
        if (this.health <= 0) {
            this.onDestroy(true);
        }
    }
}
