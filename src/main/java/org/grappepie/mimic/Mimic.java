package org.grappepie.mimic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.grappepie.mimic.commands.SpawnMimic;
import org.grappepie.mimic.listeners.DungeonChestListener;
import org.grappepie.mimic.properties.MimicChestService;

public final class Mimic extends JavaPlugin {

    public static String prefix = "&7[&bMi&3mic&7] ";
    private String version = getDescription().getVersion();
    private MimicChestService mimicChestService;

    @Override
    public void onEnable() {
        // Plugin startup logic
        mimicChestService = new MimicChestService();
        registerCommands();
        registerListeners();
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&8v" + version + " &ahas been enabled!"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&8v" + version + " &chas been disabled!"));
    }

    private void registerCommands() {
        // Register commands here
        this.getCommand("spawnmimic").setExecutor(new SpawnMimic(mimicChestService));
    }

    private void registerListeners() {
        // Register event listeners here
        Bukkit.getPluginManager().registerEvents(new DungeonChestListener(mimicChestService), this);
    }
}
