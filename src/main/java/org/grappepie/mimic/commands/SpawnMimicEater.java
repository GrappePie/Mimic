package org.grappepie.mimic.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.grappepie.mimic.properties.MimicChestEater;
import org.grappepie.mimic.properties.MimicChestService;

public class SpawnMimicEater implements CommandExecutor {

    private final MimicChestService service;

    public SpawnMimicEater(MimicChestService service) {
        this.service = service;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return true;
        }

        Block target = player.getTargetBlockExact(5);
        if (target == null
                || (target.getType() != Material.CHEST && target.getType() != Material.TRAPPED_CHEST)) {
            player.sendMessage(Component.text("Look at a chest within 5 blocks.", NamedTextColor.RED));
            return true;
        }

        if (service.getRegistry().isRegistered(target)) {
            player.sendMessage(Component.text("That chest is already a Mimic.", NamedTextColor.YELLOW));
            return true;
        }

        // Spawn in waiting state (no player eaten yet)
        MimicChestEater eater = service.createNewEater(target, null, null);
        if (eater == null) {
            player.sendMessage(Component.text("Failed to spawn Mimic Eater.", NamedTextColor.RED));
            return true;
        }

        player.sendMessage(Component.text("Mimic Eater spawned and waiting for prey.", NamedTextColor.GREEN));
        return true;
    }
}
