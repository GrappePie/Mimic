package org.grappepie.mimic.properties;

import org.bukkit.Color;
import org.bukkit.block.Block;

public class MimicChestIdle extends MimicChestPart {

    private MimicUtils.MagicCircle magicCircle;

    public MimicChestIdle(MimicChestService service, Block block) {
        super(service, block);
        this.state = MimicState.IDLE;
        this.magicCircle = new MimicUtils.MagicCircle(block, Color.WHITE, config);
        this.magicCircle.runTaskTimer(service.getPlugin(), 0, config.getMagicCircleIntervalTicks());
    }

    public void clearMagicCircle() {
        if (magicCircle != null) {
            magicCircle.cancel();
            magicCircle = null;
        }
    }

    @Override
    public void onDestroy(boolean becauseBroken) {
        if (destroyed) return;
        destroyed = true;
        clearMagicCircle();
        removeHologram();
    }

    @Override
    public void onTakeDamage(double damage) {
        onDestroy(true);
    }

    @Override
    protected void showReachArea() {
        // Idle mimics have no attack range to visualise
    }

    @Override
    protected void removeReachArea() {
        // Idle mimics have no attack range to visualise
    }
}
