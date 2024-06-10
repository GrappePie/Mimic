package org.grappepie.mimic.properties;

import org.bukkit.block.Block;

public class MimicChestIdle extends MimicChestPart {

    public MimicChestIdle(MimicChestService service, Block block) {
        super(service, block);
        updateHologram("Idle");
    }

    @Override
    public void onDestroy(boolean becauseBroken) {
        if (becauseBroken) {
            MimicChestAttacker attacker = service.createNewAttacker(block);
            if (attacker != null) {
                service.addMimic(block, attacker);
            } else {
                // Si el atacante no se pudo crear, asegúrate de marcar el MimicIdle como destruido
                destroyed = true;
                removeHologram();
            }
        } else {
            destroyed = true;
            removeHologram();
        }
    }

    @Override
    public void onTakeDamage(double damage) {
        onDestroy(true);
    }

    @Override
    protected void showReachArea() {
        // Implementar lógica para mostrar el área de alcance
    }

    @Override
    protected void removeReachArea() {
        // Implementar lógica para eliminar el área de alcance
    }
}
