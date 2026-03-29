package org.grappepie.mimic.properties;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class MimicChestAttacker extends MimicChestPart {

    private final double maxHealth;
    private final double scanRadius;
    private final Location attackLocation;
    private final boolean displayAttackZone;
    private final int maxAttackDelay;
    private int attackDelay;
    private long lastAttackTime;
    private BukkitTask attackTask;
    private BukkitTask reachAreaTask;
    private MimicUtils.MagicCircle magicCircle;

    public MimicChestAttacker(MimicChestService service, Block block, double maxHealth, Double health, double scanRadius) {
        super(service, block);
        this.state = MimicState.ATTACKER;
        this.maxHealth = maxHealth;
        this.health = health != null ? health : maxHealth;
        this.scanRadius = scanRadius;
        this.maxAttackDelay = config.getAttackerDefaultMaxAttackDelay();
        this.displayAttackZone = config.isAttackerDefaultDisplayZone();
        this.attackDelay = maxAttackDelay;
        this.attackLocation = block.getLocation().add(0.5, 1.2, 0.5);
        this.lastAttackTime = System.currentTimeMillis();

        this.magicCircle = new MimicUtils.MagicCircle(block, Color.RED, config);
        this.magicCircle.runTaskTimer(service.getPlugin(), 0, config.getMagicCircleIntervalTicks());

        Bukkit.getScheduler().runTask(service.getPlugin(),
                () -> block.getWorld().playSound(block.getLocation(), Sound.ENTITY_GHAST_SCREAM, 2, 1));

        updateHologram(this.health + " HP");

        if (displayAttackZone) {
            showReachArea();
        }

        attackTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (destroyed || block == null || !MimicUtils.isChest(block)) {
                    cancel();
                    return;
                }
                attack();
            }
        }.runTaskTimer(service.getPlugin(), 20L, 20L);
    }

    public double getMaxHealth() { return maxHealth; }
    public double getScanRadius() { return scanRadius; }

    private void attack() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime < attackDelay * 50L) return;
        lastAttackTime = currentTime;

        List<Player> nearby = getNearbyPlayers(5);
        if (!nearby.isEmpty()) {
            int rnd = ThreadLocalRandom.current().nextInt(5);
            Player target = nearby.get(ThreadLocalRandom.current().nextInt(nearby.size()));
            switch (rnd) {
                case 0 -> attackBarfZombie();
                case 1 -> attackLaunchFireball(target);
                case 2 -> attackFlameThrower(target);
                case 3 -> attackTongue(target);
                case 4 -> attackShulker(2);
            }
            return;
        }

        Player target = getRandomTarget();
        if (target == null) return;

        int rnd = ThreadLocalRandom.current().nextInt(7);
        switch (rnd) {
            case 0 -> attackLaunchFireball(target);
            case 1 -> attackBarfTNT(target);
            case 2 -> attackLaunchArrow(target);
            case 3 -> attackBarfZombie();
            case 4 -> attackTongue(target);
            case 5 -> attackShulker(5);
            case 6 -> attackSonicBoom(target);
        }
    }

    private void attackSonicBoom(Player target) {
        openChest(false);
        Bukkit.getScheduler().runTaskLater(service.getPlugin(), () -> {
            Location loc = target.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1, 1);
            loc.getWorld().spawnParticle(Particle.SONIC_BOOM, loc, 1, 0, 0, 0, 0);
            for (Player p : getNearbyPlayers(loc, 5)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
            }
            closeChest(false);
        }, 5L);
    }

    private void attackBarfZombie() {
        if (getNearbyEntities(Zombie.class, 2).size() >= 2) return;
        openChest(false);
        Bukkit.getScheduler().runTaskLater(service.getPlugin(), () -> {
            Zombie zombie = (Zombie) attackLocation.getWorld().spawnEntity(attackLocation, EntityType.ZOMBIE);
            zombie.setAge(-24000);
            zombie.setCanPickupItems(false);
            zombie.getEquipment().setHelmet(new ItemStack(Material.CHEST));
            zombie.getEquipment().setHelmetDropChance(0);
            barfEntity(zombie, 0.7);
            closeChest(false);
        }, 5L);
    }

    private void attackLaunchFireball(Player target) {
        openChest(false);
        Bukkit.getScheduler().runTaskLater(service.getPlugin(), () -> {
            Fireball fireball = attackLocation.getWorld().spawn(attackLocation, Fireball.class);
            fireball.setDirection(target.getLocation().subtract(attackLocation).toVector());
            closeChest(false);
        }, 5L);
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
        }, 5L);
    }

    private void attackTongue(Player player) {
        openChest(false);
        Bukkit.getScheduler().runTaskLater(service.getPlugin(), () -> {
            Location tongueTip = attackLocation.clone();
            List<Location> tongueSegments = new ArrayList<>();
            double stepSize = 0.4;
            int maxTicks = 60;
            int pullTicks = 30;
            World world = attackLocation.getWorld();

            new BukkitRunnable() {
                int ticksElapsed = 0;
                boolean pulling = false;

                @Override
                public void run() {
                    if (destroyed || block == null || !MimicUtils.isChest(block)) {
                        cancel();
                        return;
                    }
                    if (!pulling) {
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
                        for (Location seg : tongueSegments) {
                            world.spawnParticle(Particle.DUST, seg, 2,
                                    new Particle.DustOptions(Color.RED, 1.5f));
                        }
                        world.playSound(tongueTip, Sound.ENTITY_SLIME_SQUISH, 0.2f, 1.2f);
                        ticksElapsed++;
                    } else {
                        Vector toChest = attackLocation.clone().add(0, 0.5, 0)
                                .subtract(player.getLocation()).toVector();
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
                        world.spawnParticle(Particle.DUST, newLoc.clone().add(0, 1, 0), 8,
                                new Particle.DustOptions(Color.RED, 1.5f));
                        world.playSound(newLoc, Sound.ENTITY_SLIME_JUMP, 0.3f, 0.8f);
                        ticksElapsed++;
                    }
                }
            }.runTaskTimer(service.getPlugin(), 0L, 2L);
        }, 5L);
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
        }, 5L);
    }

    private void attackBarfTNT(Player target) {
        openChest(false);
        Bukkit.getScheduler().runTaskLater(service.getPlugin(), () -> {
            TNTPrimed tnt = attackLocation.getWorld().spawn(attackLocation, TNTPrimed.class);
            tnt.setIsIncendiary(true);
            tnt.setFuseTicks(40);
            tnt.setVelocity(target.getLocation().subtract(attackLocation).toVector().normalize().multiply(0.7));
            closeChest(false);
        }, 5L);
    }

    private void attackLaunchArrow(Player target) {
        openChest(false);
        Bukkit.getScheduler().runTaskLater(service.getPlugin(), () -> {
            Arrow arrow = attackLocation.getWorld().spawn(attackLocation, Arrow.class);
            arrow.setVelocity(target.getLocation().subtract(attackLocation).toVector().normalize().multiply(0.6));
            closeChest(false);
        }, 5L);
    }

    private void barfEntity(Entity entity, double power) {
        if (entity == null) return;
        entity.teleport(attackLocation);
        openChest(true);
        Bukkit.getScheduler().runTaskLater(service.getPlugin(), () -> {
            playBurpSound(attackLocation);
            entity.setVelocity(getBarfVector().multiply(power));
            closeChest(true);
        }, 5L);
    }

    private List<Player> getNearbyPlayers(double radius) {
        return block.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distance(block.getLocation()) <= radius)
                .collect(Collectors.toList());
    }

    private List<Player> getNearbyPlayers(Location location, double radius) {
        return location.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distance(location) <= radius)
                .collect(Collectors.toList());
    }

    private List<LivingEntity> getNearbyEntities(Class<? extends LivingEntity> entityClass, double radius) {
        return block.getWorld().getLivingEntities().stream()
                .filter(e -> entityClass.isInstance(e) && e.getLocation().distance(block.getLocation()) <= radius)
                .collect(Collectors.toList());
    }

    private Player getRandomTarget() {
        List<Player> players = getNearbyPlayers(scanRadius);
        if (players.isEmpty()) return null;
        return players.get(ThreadLocalRandom.current().nextInt(players.size()));
    }

    private Vector getBarfVector() {
        if (block.getState() instanceof Chest) {
            Directional directional = (Directional) block.getState().getBlockData();
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

    private void playBurpSound(Location loc) {
        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_BURP, 1, 1);
    }

    private void openChest(boolean silent) {
        MimicUtils.openChest(block, silent);
    }

    private void closeChest(boolean silent) {
        MimicUtils.closeChest(block, silent);
    }

    private void eatPlayer(Player player) {
        if (attackTask != null) { attackTask.cancel(); attackTask = null; }
        MimicChestEater eater = new MimicChestEater(service, block, player, health);
        service.replaceWith(block, eater);
    }

    public void clearMagicCircle() {
        if (magicCircle != null) {
            magicCircle.cancel();
            magicCircle = null;
        }
    }

    @Override
    public void onDestroy(boolean becauseDestroyed) {
        if (destroyed) return;
        destroyed = true;
        if (attackTask != null) { attackTask.cancel(); attackTask = null; }
        clearMagicCircle();
        removeHologram();
        removeReachArea();
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
    }

    @Override
    public void onTakeDamage(double damage) {
        if (health == null) health = maxHealth;
        this.health -= damage;
        updateHologram(String.format("%.1f HP", this.health));
        if (this.health <= 0) {
            this.onDestroy(true);
        }
    }

    @Override
    protected void showReachArea() {
        reachAreaTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (destroyed) { cancel(); return; }
                Location center = block.getLocation().add(0.5, 1.5, 0.5);
                for (double yAngle = 0; yAngle < 180; yAngle += 10) {
                    for (double xzAngle = 0; xzAngle < 360; xzAngle += 10) {
                        double radianY = Math.toRadians(yAngle);
                        double radianXZ = Math.toRadians(xzAngle);
                        double x = scanRadius * Math.sin(radianY) * Math.cos(radianXZ);
                        double y = scanRadius * Math.cos(radianY);
                        double z = scanRadius * Math.sin(radianY) * Math.sin(radianXZ);
                        center.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                                center.clone().add(x, y, z), 1, 0, 0, 0, 0);
                    }
                }
            }
        }.runTaskTimer(service.getPlugin(), 0L, 100L);
    }

    @Override
    protected void removeReachArea() {
        if (reachAreaTask != null) { reachAreaTask.cancel(); reachAreaTask = null; }
    }

    private void playDeathEffect(List<ItemStack> items) {
        Firework firework = attackLocation.getWorld().spawn(attackLocation, Firework.class);
        FireworkMeta fwMeta = firework.getFireworkMeta();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            FireworkEffect.Builder builder = FireworkEffect.builder();
            builder.flicker(random.nextBoolean());
            builder.trail(random.nextBoolean());
            builder.withColor(Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            fwMeta.addEffect(builder.build());
        }
        fwMeta.setPower(2);
        firework.setFireworkMeta(fwMeta);
        Bukkit.getScheduler().runTaskLater(service.getPlugin(), firework::detonate, 20L);
        Location loc = firework.getLocation();
        for (ItemStack item : items) {
            loc.getWorld().dropItem(loc, item).setVelocity(Vector.getRandom().multiply(0.3));
        }
    }
}
