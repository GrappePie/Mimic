package org.grappepie.mimic.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.grappepie.mimic.items.MimicSoul;

import java.util.List;
import java.util.stream.Collectors;

public class GiveMimicSoul implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;

    public GiveMimicSoul(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;
        int amount = 1;

        if (args.length == 0) {
            // /givemimicsoul  →  give to self
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("Specify a player: /givemimicsoul <player> [amount]", NamedTextColor.RED));
                return true;
            }
            target = (Player) sender;
        } else {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found: " + args[0], NamedTextColor.RED));
                return true;
            }
        }

        if (args.length >= 2) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount < 1 || amount > 64) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Amount must be between 1 and 64.", NamedTextColor.RED));
                return true;
            }
        }

        ItemStack soul = MimicSoul.create(plugin);
        soul.setAmount(amount);
        target.getInventory().addItem(soul);

        target.sendMessage(Component.text("You received ", NamedTextColor.GRAY)
                .append(Component.text(amount + "x ", NamedTextColor.WHITE))
                .append(Component.text("Mimic Soul", NamedTextColor.DARK_RED))
                .append(Component.text(".", NamedTextColor.GRAY)));

        if (!sender.equals(target)) {
            sender.sendMessage(Component.text("Gave ", NamedTextColor.GRAY)
                    .append(Component.text(amount + "x Mimic Soul ", NamedTextColor.DARK_RED))
                    .append(Component.text("to " + target.getName() + ".", NamedTextColor.GRAY)));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
