package org.grappepie.mimic.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.grappepie.mimic.properties.MimicChestIdle;
import org.grappepie.mimic.properties.MimicChestService;

public class SpawnMimic implements CommandExecutor {
    private final MimicChestService mimicChestService;

    public SpawnMimic(MimicChestService mimicChestService) {
        this.mimicChestService = mimicChestService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        Player player = (Player) sender;
        Block targetBlock = player.getTargetBlockExact(5); // Get the block the player is looking at within a 5 block range

        if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
            player.sendMessage(ChatColor.RED + "You need to be looking at a chest to turn it into a Mimic.");
            return true;
        }

        MimicChestIdle idleMimic = new MimicChestIdle(mimicChestService, targetBlock);
        mimicChestService.addMimic(targetBlock, idleMimic);
        player.sendMessage(ChatColor.GREEN + "A Mimic has been spawned in idle mode!");
        return true;
    }
}
