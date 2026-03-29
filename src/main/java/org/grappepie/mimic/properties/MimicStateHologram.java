package org.grappepie.mimic.properties;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;

public class MimicStateHologram {

    private final TextDisplay display;

    public MimicStateHologram(Location location, String text) {
        Location spawnLoc = location.clone().add(0.5, 2.0, 0.5);
        display = spawnLoc.getWorld().spawn(spawnLoc, TextDisplay.class, d -> {
            d.text(Component.text(text, NamedTextColor.YELLOW));
            d.setGravity(false);
            d.setPersistent(false);
            d.setBillboard(Display.Billboard.CENTER);
        });
    }

    public void updateText(String text) {
        if (display != null && !display.isDead()) {
            display.text(Component.text(text, NamedTextColor.YELLOW));
        }
    }

    public void remove() {
        if (display != null && !display.isDead()) {
            display.remove();
        }
    }
}
