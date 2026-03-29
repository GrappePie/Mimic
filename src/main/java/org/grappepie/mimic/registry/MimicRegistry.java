package org.grappepie.mimic.registry;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.grappepie.mimic.properties.MimicChestEater;
import org.grappepie.mimic.properties.MimicChestPart;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MimicRegistry {

    private final Map<Block, MimicChestPart> mimicParts = new HashMap<>();

    public void register(Block block, MimicChestPart part) {
        mimicParts.put(block, part);
    }

    public void unregister(Block block) {
        mimicParts.remove(block);
    }

    public MimicChestPart get(Block block) {
        return mimicParts.get(block);
    }

    public boolean isRegistered(Block block) {
        return mimicParts.containsKey(block);
    }

    public Collection<MimicChestPart> all() {
        return Collections.unmodifiableCollection(mimicParts.values());
    }

    public Map<Block, MimicChestPart> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(mimicParts));
    }

    public MimicChestEater getEaterForPlayer(Player player) {
        for (MimicChestPart part : mimicParts.values()) {
            if (part instanceof MimicChestEater eater && eater.getEatenPlayer() == player) {
                return eater;
            }
        }
        return null;
    }
}
