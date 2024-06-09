package org.grappepie.mimic.commands;

import org.bukkit.ChatColor;
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
            sender.sendMessage(ChatColor.RED + "Usage: /mimicdebug <true|false>");
            return false;
        }

        boolean debugMode = Boolean.parseBoolean(args[0]);
        plugin.setDebugMode(debugMode);

        sender.sendMessage(ChatColor.GREEN + "Mimic debug mode set to " + debugMode);
        return true;
    }
}
