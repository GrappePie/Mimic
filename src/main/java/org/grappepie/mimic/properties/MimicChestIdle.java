package org.grappepie.mimic.properties;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Particle;

public class MimicChestIdle extends MimicChestPart {

    public MimicChestIdle(MimicChestService service, Block block) {
        super(service, block);
        new MimicUtils.MagicCircle(block,Color.WHITE).runTaskTimer(service.getPlugin(), 0, 2);
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
            }
        } else {
            destroyed = true;
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
