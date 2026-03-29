package org.grappepie.mimic.config;

import org.bukkit.plugin.java.JavaPlugin;

public class MimicConfig {

    private final double attackerDefaultMaxHealth;
    private final int attackerDefaultMaxAttackDelay;
    private final double attackerDefaultScanRadius;
    private final boolean attackerDefaultDisplayZone;

    private final double eaterDefaultEatItemChance;
    private final int eaterDamagePeriodTicks;
    private final int eaterEatItemPeriodTicks;

    private final int magicCircleIntervalTicks;
    private final int magicCirclePointsPerEdge;
    private final double magicCircleRadius1;
    private final double magicCircleRadius2;
    private final double magicCircleRadius3;
    private final double magicCircleRadius4;

    private final double chunkMimicSpawnChance;
    private final int chunkMimicMaxY;

    public MimicConfig(JavaPlugin plugin) {
        plugin.saveDefaultConfig();

        attackerDefaultMaxHealth = plugin.getConfig().getDouble("attacker.default-max-health", 20.0);
        attackerDefaultMaxAttackDelay = plugin.getConfig().getInt("attacker.default-max-attack-delay", 40);
        attackerDefaultScanRadius = plugin.getConfig().getDouble("attacker.default-scan-radius", 12.0);
        attackerDefaultDisplayZone = plugin.getConfig().getBoolean("attacker.display-attack-zone", false);

        eaterDefaultEatItemChance = plugin.getConfig().getDouble("eater.default-eat-item-chance", 0.5);
        eaterDamagePeriodTicks = plugin.getConfig().getInt("eater.damage-period-ticks", 400);
        eaterEatItemPeriodTicks = plugin.getConfig().getInt("eater.eat-item-period-ticks", 400);

        magicCircleIntervalTicks = plugin.getConfig().getInt("magic-circle.interval-ticks", 4);
        magicCirclePointsPerEdge = plugin.getConfig().getInt("magic-circle.points-per-edge", 10);
        magicCircleRadius1 = plugin.getConfig().getDouble("magic-circle.radius-1", 1.5);
        magicCircleRadius2 = plugin.getConfig().getDouble("magic-circle.radius-2", 2.0);
        magicCircleRadius3 = plugin.getConfig().getDouble("magic-circle.radius-3", 2.5);
        magicCircleRadius4 = plugin.getConfig().getDouble("magic-circle.radius-4", 3.0);

        chunkMimicSpawnChance = plugin.getConfig().getDouble("chunk-spawn.chance", 0.2);
        chunkMimicMaxY = plugin.getConfig().getInt("chunk-spawn.max-y", 60);
    }

    public double getAttackerDefaultMaxHealth() { return attackerDefaultMaxHealth; }
    public int getAttackerDefaultMaxAttackDelay() { return attackerDefaultMaxAttackDelay; }
    public double getAttackerDefaultScanRadius() { return attackerDefaultScanRadius; }
    public boolean isAttackerDefaultDisplayZone() { return attackerDefaultDisplayZone; }

    public double getEaterDefaultEatItemChance() { return eaterDefaultEatItemChance; }
    public int getEaterDamagePeriodTicks() { return eaterDamagePeriodTicks; }
    public int getEaterEatItemPeriodTicks() { return eaterEatItemPeriodTicks; }

    public int getMagicCircleIntervalTicks() { return magicCircleIntervalTicks; }
    public int getMagicCirclePointsPerEdge() { return magicCirclePointsPerEdge; }
    public double getMagicCircleRadius1() { return magicCircleRadius1; }
    public double getMagicCircleRadius2() { return magicCircleRadius2; }
    public double getMagicCircleRadius3() { return magicCircleRadius3; }
    public double getMagicCircleRadius4() { return magicCircleRadius4; }

    public double getChunkMimicSpawnChance() { return chunkMimicSpawnChance; }
    public int getChunkMimicMaxY() { return chunkMimicMaxY; }
}
