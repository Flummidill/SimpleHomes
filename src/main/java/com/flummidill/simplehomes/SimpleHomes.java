package com.flummidill.simplehomes;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleHomes extends JavaPlugin {

    private HomeManager homeManager;

    @Override
    public void onEnable() {
        getLogger().info("~ Created by Flummidill ~");

        // Load Configuration
        getLogger().info("Loading Configuration...");
        saveDefaultConfig();
        validateConfig();

        // Initialize Home-Manager
        getLogger().info("Initializing Home-Manager...");
        homeManager = new HomeManager(this);

        // Register Commands
        getLogger().info("Registering Commands...");
        CommandHandler commandHandler = new CommandHandler(this, homeManager);
        TabCompleter tabCompleter = new TabCompleter(homeManager);

        getCommand("sethome").setExecutor(commandHandler);
        getCommand("home").setExecutor(commandHandler);
        getCommand("delhome").setExecutor(commandHandler);
        getCommand("homeadmin").setExecutor(commandHandler);

        getCommand("sethome").setTabCompleter(tabCompleter);
        getCommand("home").setTabCompleter(tabCompleter);
        getCommand("delhome").setTabCompleter(tabCompleter);
        getCommand("homeadmin").setTabCompleter(tabCompleter);
    }

    private void validateConfig() {
        FileConfiguration config = getConfig();
        int maxHomes = config.getInt("max-homes", 3);
        if (maxHomes < 1 || maxHomes > 50) {
            getLogger().warning("Configuration Error: \"max-homes\" was configured incorrectly and reset to 3.");
            config.set("max-homes", 3);
            saveConfig();
        }
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }
}