package org.grappepie.mimic.properties;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class MimicStateHologram {
    private final ArmorStand hologram;

    public MimicStateHologram(Location location, String text) {
        hologram = (ArmorStand) location.getWorld().spawnEntity(location.add(0.5, 2, 0.5), EntityType.ARMOR_STAND);
        hologram.setCustomName(text);
        hologram.setCustomNameVisible(true);
        hologram.setInvisible(true);
        hologram.setInvulnerable(true);
        hologram.setGravity(false);
        hologram.setMarker(true);
    }

    public void updateText(String text) {
        hologram.setCustomName(text);
    }

    public void remove() {
        hologram.remove();
    }
}
