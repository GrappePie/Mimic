package org.grappepie.mimic.properties;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.Location;

public abstract class MimicChestPart {
    protected boolean destroyed;
    public final Block block;
    protected final Location mount;
    protected final Chest chest;
    protected final MimicChestService service;
    protected MimicStateHologram hologram;
    protected MimicState state; // Agregar este campo
    Double health;

    public MimicChestPart(MimicChestService service, Block block) {
        this.service = service;
        this.block = block;
        this.mount = block.getLocation().add(0.5, 1.2, 0.5);

        if (!(block.getState() instanceof Chest)) {
            throw new RuntimeException("Can't create MimicPart for non-chest block [" + block.getX() + ":" + block.getY() + ":" + block.getZ() + "]");
        }
        this.chest = (Chest) block.getState();
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

    public MimicState getState() { // Agregar este método
        return state;
    }

    public void setState(MimicState state) { // Agregar este método
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
}
