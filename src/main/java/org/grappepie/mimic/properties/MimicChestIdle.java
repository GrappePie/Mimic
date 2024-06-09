package org.grappepie.mimic.properties;

import org.bukkit.block.Block;

public class MimicChestIdle extends MimicChestPart {

    public MimicChestIdle(MimicChestService service, Block block) {
        super(service, block);
        this.state = MimicState.IDLE;
        updateHologram("Idle");
    }

    @Override
    public void onTakeDamage(double damage) {
        // Comportamiento del Mimic en modo Idle cuando recibe daño
    }

    @Override
    public void onDestroy(boolean becauseBroken) {
        // Comportamiento del Mimic en modo Idle cuando se destruye
        destroyed = true;
        removeHologram();
    }

    @Override
    protected void showReachArea() {
        // Lógica para mostrar el área de alcance (si aplica)
    }

    @Override
    protected void removeReachArea() {
        // Lógica para eliminar el área de alcance (si aplica)
    }
}
