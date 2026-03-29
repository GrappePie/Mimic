package org.grappepie.mimic.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.grappepie.mimic.Mimic;

public class MimicDebugCommand implements CommandExecutor {

    private final Mimic plugin;

    public MimicDebugCommand(Mimic plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(Component.text("Usage: /mimicdebug <true|false>", NamedTextColor.RED));
            return false;
        }
        if (args[0].equalsIgnoreCase("true")) {
            plugin.setDebugMode(true);
            sender.sendMessage(Component.text("Debug mode enabled.", NamedTextColor.GREEN));
        } else if (args[0].equalsIgnoreCase("false")) {
            plugin.setDebugMode(false);
            sender.sendMessage(Component.text("Debug mode disabled.", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Usage: /mimicdebug <true|false>", NamedTextColor.RED));
            return false;
        }
        return true;
    }
}
