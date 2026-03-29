package org.grappepie.mimic.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.grappepie.mimic.properties.MimicChestAttacker;
import org.grappepie.mimic.properties.MimicChestService;

public class SpawnMimicAttacker implements CommandExecutor {

    private final MimicChestService service;

    public SpawnMimicAttacker(MimicChestService service) {
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

        double maxHealth = service.getConfig().getAttackerDefaultMaxHealth();
        double scanRadius = service.getConfig().getAttackerDefaultScanRadius();

        if (args.length >= 1) {
            try { maxHealth = Double.parseDouble(args[0]); } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Invalid maxHealth value.", NamedTextColor.RED));
                return true;
            }
        }
        if (args.length >= 2) {
            try { scanRadius = Double.parseDouble(args[1]); } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Invalid scanRadius value.", NamedTextColor.RED));
                return true;
            }
        }

        MimicChestAttacker attacker = service.createNewAttacker(target, maxHealth, null, scanRadius);
        if (attacker == null) {
            player.sendMessage(Component.text("Failed to spawn Mimic Attacker.", NamedTextColor.RED));
            return true;
        }

        player.sendMessage(Component.text(
                "Mimic Attacker spawned! HP=" + maxHealth + " Radius=" + scanRadius,
                NamedTextColor.GREEN));
        return true;
    }
}
