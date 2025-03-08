package com.example.spawnmanager.config;

import com.example.spawnmanager.SpawnManagerPlugin;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final SpawnManagerPlugin plugin;

    @Getter
    private Location spawnLocation;

    @Getter
    private final Map<String, WorldCleaningSettings> worldSettings;

    @Getter
    private boolean messageEnabled;

    @Getter
    private String spawnMessage;

    @Getter
    private boolean timerEnabled;

    @Getter
    private int timerSeconds;

    @Getter
    private boolean cancelOnMove;

    @Getter
    private String countdownMessage;

    @Getter
    private String cancelledMessage;

    public ConfigManager(final SpawnManagerPlugin plugin) {
        this.plugin = plugin;
        this.worldSettings = new HashMap<>();
        this.loadConfig();
    }

    public void loadConfig() {
        // Load spawn location
        final ConfigurationSection spawnSection = this.plugin.getConfig().getConfigurationSection("spawn");
        if (spawnSection != null) {
            final World world = this.plugin.getServer().getWorld(spawnSection.getString("world", "world"));
            if (world != null) {
                this.spawnLocation = new Location(
                        world,
                        spawnSection.getDouble("x", 0.0),
                        spawnSection.getDouble("y", 64.0),
                        spawnSection.getDouble("z", 0.0),
                        (float) spawnSection.getDouble("yaw", 0.0),
                        (float) spawnSection.getDouble("pitch", 0.0)
                );
            }

            this.messageEnabled = spawnSection.getBoolean("message-enabled", true);
            this.spawnMessage = spawnSection.getString("message", "§aTeleported to spawn!");

            final ConfigurationSection timerSection = spawnSection.getConfigurationSection("timer");
            if (timerSection != null) {
                this.timerEnabled = timerSection.getBoolean("enabled", true);
                this.timerSeconds = timerSection.getInt("seconds", 3);
                this.cancelOnMove = timerSection.getBoolean("cancel-on-move", true);
                this.countdownMessage = timerSection.getString("countdown-message", "§eTeleporting to spawn in %seconds% seconds...");
                this.cancelledMessage = timerSection.getString("cancelled-message", "§cTeleport cancelled due to movement!");
            }
        }

        // Load world-specific settings
        final ConfigurationSection worldsSection = this.plugin.getConfig().getConfigurationSection("worlds");
        if (worldsSection != null) {
            for (final String worldName : worldsSection.getKeys(false)) {
                final ConfigurationSection worldSection = worldsSection.getConfigurationSection(worldName);
                if (worldSection != null) {
                    this.worldSettings.put(worldName, new WorldCleaningSettings(
                            worldSection.getBoolean("clean-mythic-mobs", true),
                            worldSection.getBoolean("clean-items", true),
                            worldSection.getBoolean("clean-entities", true),
                            worldSection.getBoolean("clean-holograms", true),
                            worldSection.getBoolean("clean-projectiles", true)
                    ));
                }
            }
        }
    }

    public WorldCleaningSettings getWorldSettings(final String worldName) {
        // Return the world's specific settings if they exist, otherwise return default settings (all true)
        return this.worldSettings.getOrDefault(worldName, new WorldCleaningSettings(true, true, true, true, true));
    }
}