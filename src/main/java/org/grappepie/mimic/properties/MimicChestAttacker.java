package org.grappepie.mimic.properties;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class MimicChestAttacker extends MimicChestPart {
    private final double maxHealth;
    private Timer attackTimer;
    private final double scanRadius;
    private final Location attackLocation;
    private final boolean displayAttackZone;
    private final int maxAttackDelay;
    private int attackDelay;
    private TimerTask reachAreaTask;
    private long lastAttackTime;
    private MimicUtils.MagicCircle magicCircle;

    public MimicChestAttacker(MimicChestService service, Block block, Map<String, Object> params) {
        super(service, block);
        this.magicCircle = new MimicUtils.MagicCircle(block,Color.RED);
        this.magicCircle.runTaskTimer(service.getPlugin(), 0, 2);
        this.maxHealth = params.get("maxHealth") != null ? (Double) params.get("maxHealth") : 20.0;
        this.health = params.get("health") != null ? (Double) params.get("health") : maxHealth;
        this.maxAttackDelay = params.get("maxAttackDelay") != null ? (Integer) params.get("maxAttackDelay") : 40;
        this.scanRadius = params.get("scanRadius") != null ? (Double) params.get("scanRadius") : 12.0;
        this.displayAttackZone = params.get("displayAttackZone") != null ? (Boolean) params.get("displayAttackZone") : false;

        this.attackDelay = maxAttackDelay;
        this.attackLocation = block.getLocation().add(0.5, 1.2, 0.5);
        this.lastAttackTime = System.currentTimeMillis();
        Bukkit.getScheduler().runTask(service.getPlugin(), () -> block.getWorld().playSound(block.getLocation(), Sound.ENTITY_GHAST_SCREAM, 2, 1));

        updateHologram(health + " HP");

        if (displayAttackZone) {
            displayAttackZone();
        }

        this.attackTimer = new Timer();
        attackTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(service.getPlugin(), () -> attack());
            }
        }, 20, 20);
    }

    private void displayAttackZone() {
        // Implementar lógica para mostrar la zona de ataque
    }

    private void attack() {
        // Verifica si el cofre sigue existiendo y es válido
        if (block == null || !block.getType().name().contains("CHEST")) {
            // Cancela el timer de ataque y elimina el holograma si existe
            if (attackTimer != null) {
                attackTimer.cancel();
                attackTimer = null;
            }
            removeHologram();
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime < attackDelay * 50) {
            return; // No hacer nada si no ha pasado suficiente tiempo desde el último ataque
        }

        lastAttackTime = currentTime; // Actualizar el tiempo del último ataque
        List<Player> playersAroundChest = getNearbyPlayers(5);

        if (!playersAroundChest.isEmpty()) {
            int rnd = ThreadLocalRandom.current().nextInt(5);
            switch (rnd) {
                case 0:
                    attackBarfZombie();
                    break;
                case 1:
                    attackLaunchFireball(playersAroundChest.get(ThreadLocalRandom.current().nextInt(playersAroundChest.size())));
                    break;
                case 2:
                    attackFlameThrower(playersAroundChest.get(ThreadLocalRandom.current().nextInt(playersAroundChest.size())));
                    break;
                case 3:
                    attackTongue(playersAroundChest.get(ThreadLocalRandom.current().nextInt(playersAroundChest.size())));
                    break;
                case 4:
                    attackShulker(2);
                    break;
            }
            return;
        }

        Player target = getRandomTarget();
        if (target == null) {
            return;
        }

        int rnd = ThreadLocalRandom.current().nextInt(7);
        switch (rnd) {
            case 0:
                attackLaunchFireball(target);
                break;
            case 1:
                attackBarfTNT(target);
                break;
            case 2:
                attackLaunchArrow(target);
                break;
            case 3:
                attackBarfZombie();
                break;
            case 4:
                attackTongue(target);
                break;
            case 5:
                attackShulker(5);
                break;
            case 6:
                attackSonicBoom(target);
                break;
        }
    }

    private void attackSonicBoom(Player target) {
        openChest(false);
        Bukkit.getScheduler().runTaskLater(service.getPlugin(), () -> {
            Location loc = target.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1, 1);
            loc.getWorld().spawnParticle(Particle.SONIC_BOOM, loc, 1, 0, 0, 0, 0);
            for (Player player : getNearbyPlayers(loc, 5)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
            }
            closeChest(false);
        }, 5);
    }

    private void attackBarfZombie() {
        if (getNearbyEntities(Zombie.class, 2).size() >= 2) {
            return;
        }
        openChest(false);
        Bukkit.getScheduler().runTaskLater(service.getPlugin(), () -> {
            Zombie zombie = (Zombie) attackLocation.getWorld().spawnEntity(attackLocation, EntityType.ZOMBIE);
            zombie.setAge(-24000);
            zombie.setCanPickupItems(false);
            zombie.getEquipment().setHelmet(new ItemStack(Material.CHEST));
            zombie.getEquipment().setHelmetDropChance(0);
            barfEntity(zombie, 0.7);
            closeChest(false);
        }, 5);
    }

    private void attackLaunchFireball(Player target) {
        openChest(false);
        Bukkit.getScheduler().runTaskLater(service.getPlugin(), () -> {
            Fireball fireball = attackLocation.getWorld().spawn(attackLocation, Fireball.class);
            fireball.setDirection(target.getLocation().subtract(attackLocation).toVector());
            closeChest(false);
        }, 5);
    }

    private void attackFlameThrower(Player player) {
        openChest(false);
        Bukkit.getScheduler().runTaskLater(service.getPlugin(), () -> {
            Location loc = player.getEyeLocation().add(player.getLocation()).multiply(0.5);
            Vector vector = loc.toVector().subtract(attackLocation.toVector()).normalize();
            for (double i = 0; i < 5; i += 0.4) {
                Location particleLoc = attackLocation.clone().add(vector.clone().multiply(i));
                attackLocation.getWorld().spawnParticle(Particle.FLAME, particleLoc, 0, 0.3, 0.3, 0.3, 0);
                if (getNearbyPlayers(particleLoc, 1.2).contains(player)) {
                    player.setFireTicks(200);
                }
            }
            closeChest(false);
        }, 5);
    }

    private void attackTongue(Player player) {
        openChest(false);
        Bukkit.getScheduler().runTaskLater(service.getPlugin(), () -> {
            Location tongueTip = attackLocation.clone();
            List<Location> tongueSegments = new ArrayList<>();
            double stepSize = 0.4; // Tamaño de cada segmento de la lengua
            int maxTicks = 60; // Duración máxima de la animación de la lengua
            int pullTicks = 30; // Ticks para arrastrar al jugador
            World world = attackLocation.getWorld();
            new BukkitRunnable() {
                int ticksElapsed = 0;
                boolean pulling = false;
                @Override
                public void run() {
                    if (!pulling) {
                        // Animación de la lengua avanzando
                        Vector toPlayer = player.getLocation().add(0, 1, 0).subtract(tongueTip).toVector();
                        double distance = toPlayer.length();
                        if (distance < stepSize || ticksElapsed > maxTicks) {
                            pulling = true;
                            world.playSound(player.getLocation(), Sound.ENTITY_FROG_TONGUE, 1, 1);
                            return;
                        }
                        Vector step = toPlayer.normalize().multiply(stepSize);
                        tongueTip.add(step);
                        tongueSegments.add(tongueTip.clone());
                        // Dibuja la lengua con partículas rojas
                        for (Location seg : tongueSegments) {
                            world.spawnParticle(Particle.DUST, seg, 2, new Particle.DustOptions(Color.RED, 1.5f));
                        }
                        world.playSound(tongueTip, Sound.ENTITY_SLIME_SQUISH, 0.2f, 1.2f);
                        ticksElapsed++;
                    } else {
                        // Arrastre del jugador hacia el cofre
                        Vector toChest = attackLocation.clone().add(0, 0.5, 0).subtract(player.getLocation()).toVector();
                        double dist = toChest.length();
                        if (dist < 0.7 || ticksElapsed > maxTicks + pullTicks) {
                            eatPlayer(player);
                            closeChest(false);
                            cancel();
                            return;
                        }
                        Vector pull = toChest.normalize().multiply(0.4);
                        Location newLoc = player.getLocation().add(pull);
                        player.teleport(newLoc);
                        world.spawnParticle(Particle.DUST, newLoc.add(0, 1, 0), 8, new Particle.DustOptions(Color.RED, 1.5f));
                        world.playSound(newLoc, Sound.ENTITY_SLIME_JUMP, 0.3f, 0.8f);
                        ticksElapsed++;
                    }
                    // Verifica si el cofre sigue existiendo y es válido durante la animación
                    if (block == null || !block.getType().name().contains("CHEST")) {
                        cancel();
                        return;
                    }
                }
            }.runTaskTimer(service.getPlugin(), 0, 2); // Cada 2 ticks para suavidad
        }, 5);
    }


    private void attackShulker(int bulletCount) {
        openChest(false);
        Bukkit.getScheduler().runTaskLater(service.getPlugin(), () -> {
            for (int i = 0; i < bulletCount; i++) {
                Player target = getRandomTarget();
                if (target != null) {
                    ShulkerBullet bullet = attackLocation.getWorld().spawn(attackLocation, ShulkerBullet.class);
                    bullet.setTarget(target);
                }
            }
            closeChest(false);
        }, 5);
    }

    private void attackBarfTNT(Player target) {
        openChest(false);
        Bukkit.getScheduler().runTaskLater(service.getPlugin(), () -> {
            TNTPrimed tnt = attackLocation.getWorld().spawn(attackLocation, TNTPrimed.class);
            tnt.setIsIncendiary(true);
            tnt.setFuseTicks(40);
            tnt.setVelocity(target.getLocation().subtract(attackLocation).toVector().normalize().multiply(0.7));
            closeChest(false);
        }, 5);
    }

    private void attackLaunchArrow(Player target) {
        openChest(false);
        Bukkit.getScheduler().runTaskLater(service.getPlugin(), () -> {
            Arrow arrow = attackLocation.getWorld().spawn(attackLocation, Arrow.class);
            arrow.setVelocity(target.getLocation().subtract(attackLocation).toVector().normalize().multiply(0.6));
            closeChest(false);
        }, 5);
    }

    private void barfEntity(Entity entity, double power) {
        if (entity == null) {
            return;
        }
        entity.teleport(attackLocation);
        openChest(true);
        Bukkit.getScheduler().runTaskLater(service.getPlugin(), () -> {
            playBurpSound(attackLocation);
            entity.setVelocity(getBarfVector().multiply(power));
            closeChest(true);
        }, 5);
    }

    private List<Player> getNearbyPlayers(double radius) {
        return block.getWorld().getPlayers().stream()
                .filter(player -> player.getLocation().distance(block.getLocation()) <= radius)
                .collect(Collectors.toList());
    }

    private List<Player> getNearbyPlayers(Location location, double radius) {
        return location.getWorld().getPlayers().stream()
                .filter(player -> player.getLocation().distance(location) <= radius)
                .collect(Collectors.toList());
    }

    private List<LivingEntity> getNearbyEntities(Class<? extends LivingEntity> entityClass, double radius) {
        return block.getWorld().getLivingEntities().stream()
                .filter(entity -> entityClass.isInstance(entity) && entity.getLocation().distance(block.getLocation()) <= radius)
                .collect(Collectors.toList());
    }

    private Player getRandomTarget() {
        List<Player> players = getNearbyPlayers(scanRadius);
        if (players.isEmpty()) {
            return null;
        }
        return players.get(ThreadLocalRandom.current().nextInt(players.size()));
    }

    private Vector getBarfVector() {
        // Use the chest's direction to determine the barf vector
        if (block.getState() instanceof Chest) {
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

    private void playBurpSound(Location loc) {
        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_BURP, 1, 1);
    }

    @Override
    public void onDestroy(boolean becauseDestroyed) {
        destroyed = true;
        if (becauseDestroyed) {
            if (!(block.getState() instanceof Chest)) return;
            block.getWorld().playSound(block.getLocation(), Sound.ENTITY_ZOMBIE_HORSE_DEATH, 1, 1);
            Chest chest = (Chest) block.getState();
            List<ItemStack> items = new ArrayList<>(Arrays.asList(chest.getInventory().getContents()));
            chest.getInventory().clear();
            if (!items.isEmpty()) items.remove(0);
            items = items.stream().filter(Objects::nonNull).collect(Collectors.toList());
            block.setType(Material.AIR);
            block.getWorld().createExplosion(block.getLocation(), 2);
            playDeathEffect(items);
        }
        if (attackTimer != null) {
            attackTimer.cancel();
        }
        removeHologram();
        removeReachArea();
        clearMagicCircle();
    }

    @Override
    public void onTakeDamage(double damage) {
        this.health -= damage;
        updateHologram(health + " HP");
        if (this.health <= 0) {
            this.onDestroy(true);
        }
    }

    @Override
    protected void showReachArea() {
        reachAreaTask = new TimerTask() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(service.getPlugin(), () -> {
                    double radius = scanRadius;
                    Location center = block.getLocation().add(0.5, 1.5, 0.5);

                    for (double yAngle = 0; yAngle < 180; yAngle += 10) {
                        for (double xzAngle = 0; xzAngle < 360; xzAngle += 10) {
                            double radianY = Math.toRadians(yAngle);
                            double radianXZ = Math.toRadians(xzAngle);

                            double x = radius * Math.sin(radianY) * Math.cos(radianXZ);
                            double y = radius * Math.cos(radianY);
                            double z = radius * Math.sin(radianY) * Math.sin(radianXZ);

                            Location particleLocation = center.clone().add(x, y, z);
                            center.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, particleLocation, 1, 0, 0, 0, 0);
                        }
                    }
                });
            }
        };
        attackTimer.schedule(reachAreaTask, 0, 20 * 5); // Repite cada 5 segundos
    }

    @Override
    protected void removeReachArea() {
        if (reachAreaTask != null) {
            reachAreaTask.cancel();
            reachAreaTask = null;
        }
    }

    public void clearMagicCircle() {
        if (magicCircle != null) {
            magicCircle.cancel();
            magicCircle = null;
        }
    }

    private void playDeathEffect(List<ItemStack> items) {
        Firework firework = attackLocation.getWorld().spawn(attackLocation, Firework.class);
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
        Bukkit.getScheduler().runTaskLater(service.getPlugin(), firework::detonate, 20);
        Location loc = firework.getLocation();
        for (ItemStack item : items) {
            loc.getWorld().dropItem(loc, item).setVelocity(Vector.getRandom().multiply(0.3));
        }
    }

    private void openChest(boolean silent) {
        Bukkit.getScheduler().runTask(service.getPlugin(), () -> MimicUtils.openChest(block, silent));
    }

    private void closeChest(boolean silent) {
        Bukkit.getScheduler().runTask(service.getPlugin(), () -> MimicUtils.closeChest(block, silent));
    }

    private void eatPlayer(Player player) {
        Bukkit.getScheduler().runTask(service.getPlugin(), () -> {
            MimicChestEater eater = new MimicChestEater(service, block, player, new Timer(), health);
            service.addMimic(block, eater);
        });
    }
}
