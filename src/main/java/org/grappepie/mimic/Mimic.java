package org.grappepie.mimic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.grappepie.mimic.commands.*;
import org.grappepie.mimic.config.MimicConfig;
import org.grappepie.mimic.listeners.*;
import org.grappepie.mimic.persistence.MimicPersistence;
import org.grappepie.mimic.properties.MimicChestService;
import org.grappepie.mimic.properties.MimicChestPart;
import org.grappepie.mimic.registry.MimicRegistry;

public final class Mimic extends JavaPlugin {

    private MimicChestService service;
    private MimicRegistry registry;
    private MimicPersistence persistence;
    private boolean debugMode = false;

    @Override
    public void onEnable() {
        MimicConfig config = new MimicConfig(this);
        registry = new MimicRegistry();
        service = new MimicChestService(this, config, registry);

        persistence = new MimicPersistence(this, registry, config, service);
        persistence.load();

        registerCommands();
        registerListeners();

        getServer().getConsoleSender().sendMessage(
                Component.text("[Mimic] ", NamedTextColor.AQUA)
                        .append(Component.text("v" + getDescription().getVersion()
                                + " enabled.", NamedTextColor.GREEN)));
    }

    @Override
    public void onDisable() {
        if (persistence != null) persistence.save();

        // Cleanly destroy all active mimics so tasks and holograms are removed
        if (registry != null) {
            for (MimicChestPart part : registry.all()) {
                if (!part.isDestroyed()) part.onDestroy(false);
            }
        }

        getServer().getConsoleSender().sendMessage(
                Component.text("[Mimic] ", NamedTextColor.AQUA)
                        .append(Component.text("v" + getDescription().getVersion()
                                + " disabled.", NamedTextColor.RED)));
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        service.updateDebugMode(debugMode);
    }

    private void registerCommands() {
        getCommand("spawnmimic").setExecutor(new SpawnMimic(service));
        getCommand("spawnmimicattacker").setExecutor(new SpawnMimicAttacker(service));
        getCommand("spawnmimiceater").setExecutor(new SpawnMimicEater(service));
        getCommand("mimicdebug").setExecutor(new MimicDebugCommand(this));
        getCommand("mimicreload").setExecutor((sender, cmd, label, args) -> {
            reloadConfig();
            sender.sendMessage(Component.text("Mimic config reloaded. Restart for full effect.",
                    NamedTextColor.YELLOW));
            return true;
        });
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new MimicChestListener(service, registry), this);
        Bukkit.getPluginManager().registerEvents(new MimicChestEaterListener(registry), this);
        Bukkit.getPluginManager().registerEvents(new MimicChestAttackerListener(registry), this);
    }
}
