package org.grappepie.mimic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.grappepie.mimic.commands.MimicDebugCommand;
import org.grappepie.mimic.commands.SpawnMimic;
import org.grappepie.mimic.listeners.*;
import org.grappepie.mimic.properties.MimicChestService;

public final class Mimic extends JavaPlugin {

    public static String prefix = "&7[&bMi&3mic&7] ";
    private String version = getDescription().getVersion();
    private MimicChestService mimicChestService;
    private boolean debugMode = false; // Añadido para controlar el modo de depuración

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

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        mimicChestService.updateDebugMode(debugMode);
    }

    private void registerCommands() {
        // Register commands here
        this.getCommand("spawnmimic").setExecutor(new SpawnMimic(mimicChestService));
        this.getCommand("mimicdebug").setExecutor(new MimicDebugCommand(this));
    }

    private void registerListeners() {
        // Register event listeners here
        Bukkit.getPluginManager().registerEvents(new MimicChestListener(mimicChestService), this);
        Bukkit.getPluginManager().registerEvents(new MimicChestIdleListener(mimicChestService), this);
        Bukkit.getPluginManager().registerEvents(new MimicChestEaterListener(mimicChestService), this);
        Bukkit.getPluginManager().registerEvents(new MimicChestAttackerListener(mimicChestService), this);
    }
}
