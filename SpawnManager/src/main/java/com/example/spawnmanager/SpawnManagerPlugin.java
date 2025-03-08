package com.example.spawnmanager;

import com.example.spawnmanager.commands.SpawnCommand;
import com.example.spawnmanager.config.ConfigManager;
import com.example.spawnmanager.service.WorldCleanerService;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnManagerPlugin extends JavaPlugin {
    
    @Getter
    private static SpawnManagerPlugin instance;
    
    @Getter
    private ConfigManager configManager;
    
    @Getter
    private WorldCleanerService worldCleanerService;
    
    @Getter
    private SpawnCommand spawnCommand;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize configuration
        this.saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        
        // Initialize services
        this.worldCleanerService = new WorldCleanerService(this);
        
        // Register commands
        this.spawnCommand = new SpawnCommand(this);
        this.getCommand("spawn").setExecutor(this.spawnCommand);
        
        this.getLogger().info("SpawnManager has been enabled!");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("SpawnManager has been disabled!");
    }
}