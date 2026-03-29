package org.grappepie.mimic.properties;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.grappepie.mimic.config.MimicConfig;

public abstract class MimicChestPart {

    protected boolean destroyed = false;
    public final Block block;
    protected final Location mount;
    protected final MimicChestService service;
    protected final MimicConfig config;
    protected MimicStateHologram hologram;
    protected MimicState state;
    protected boolean debugMode = false;
    Double health;

    public MimicChestPart(MimicChestService service, Block block) {
        this.service = service;
        this.block = block;
        this.config = service.getConfig();
        this.mount = block.getLocation().add(0.5, 1.2, 0.5);

        if (!(block.getState() instanceof Chest)) {
            throw new RuntimeException("Cannot create MimicPart on a non-chest block ["
                    + block.getX() + ":" + block.getY() + ":" + block.getZ() + "]");
        }
    }

    public void updateHologram(String text) {
        if (hologram == null) {
            hologram = new MimicStateHologram(block.getLocation(), text);
        } else {
            hologram.updateText(text);
        }
    }

    public void removeHologram() {
        if (hologram != null) {
            hologram.remove();
            hologram = null;
        }
    }

    public MimicState getState() {
        return state;
    }

    public void setState(MimicState state) {
        this.state = state;
    }

    public Double getHealth() {
        return health;
    }

    public void setHealth(Double health) {
        this.health = health;
    }

    public abstract void onTakeDamage(double damage);

    public abstract void onDestroy(boolean becauseBroken);

    public final boolean isDestroyed() {
        return destroyed;
    }

    public void updateDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        if (debugMode) {
            showReachArea();
        } else {
            removeReachArea();
        }
    }

    protected abstract void showReachArea();

    protected abstract void removeReachArea();
}
