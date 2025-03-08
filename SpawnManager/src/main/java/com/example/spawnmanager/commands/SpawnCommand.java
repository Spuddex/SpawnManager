package com.example.spawnmanager.commands;

import com.example.spawnmanager.SpawnManagerPlugin;
import com.example.spawnmanager.config.WorldCleaningSettings;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnCommand implements CommandExecutor, Listener {

    private final SpawnManagerPlugin plugin;
    private final Map<UUID, BukkitTask> pendingTeleports;
    private final Map<UUID, Location> startLocations;

    public SpawnCommand(final SpawnManagerPlugin plugin) {
        this.plugin = plugin;
        this.pendingTeleports = new HashMap<>();
        this.startLocations = new HashMap<>();
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        return this.teleportToSpawn(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(final PlayerCommandPreprocessEvent event) {
        final String command = event.getMessage().toLowerCase();
        if (command.equals("/spawn") || command.startsWith("/spawn ")) {
            event.setCancelled(true);
            this.teleportToSpawn(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (!this.plugin.getConfigManager().isCancelOnMove()) {
            return;
        }

        final Player player = event.getPlayer();
        final UUID playerId = player.getUniqueId();

        if (!this.pendingTeleports.containsKey(playerId)) {
            return;
        }

        final Location startLoc = this.startLocations.get(playerId);
        final Location currentLoc = event.getTo();

        if (startLoc.getBlockX() != currentLoc.getBlockX()
                || startLoc.getBlockY() != currentLoc.getBlockY()
                || startLoc.getBlockZ() != currentLoc.getBlockZ()) {
            this.cancelTeleport(player);
            player.sendMessage(this.plugin.getConfigManager().getCancelledMessage());
        }
    }

    private void cancelTeleport(final Player player) {
        final UUID playerId = player.getUniqueId();
        final BukkitTask task = this.pendingTeleports.remove(playerId);
        if (task != null) {
            task.cancel();
        }
        this.startLocations.remove(playerId);
    }

    private boolean teleportToSpawn(final Player player) {
        final Location spawnLocation = this.plugin.getConfigManager().getSpawnLocation();
        if (spawnLocation == null) {
            player.sendMessage("§cSpawn location has not been set!");
            return true;
        }

        final UUID playerId = player.getUniqueId();

        // Cancel any existing teleport
        this.cancelTeleport(player);

        if (!this.plugin.getConfigManager().isTimerEnabled()) {
            this.executeSpawnTeleport(player, spawnLocation);
            return true;
        }

        // Store starting location for movement check
        this.startLocations.put(playerId, player.getLocation().clone());

        // Start teleport timer
        final int seconds = this.plugin.getConfigManager().getTimerSeconds();
        final String countdownMessage = this.plugin.getConfigManager().getCountdownMessage();

        final BukkitTask task = new BukkitRunnable() {
            private int timeLeft = seconds;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    this.cancel();
                    pendingTeleports.remove(playerId);
                    startLocations.remove(playerId);
                    executeSpawnTeleport(player, spawnLocation);
                    return;
                }

                player.sendMessage(countdownMessage.replace("%seconds%", String.valueOf(timeLeft)));
                timeLeft--;
            }
        }.runTaskTimer(this.plugin, 0L, 20L);

        this.pendingTeleports.put(playerId, task);
        return true;
    }

    private void executeSpawnTeleport(final Player player, final Location spawnLocation) {
        // Clean the current world before teleporting
        final String worldName = player.getWorld().getName();
        final WorldCleaningSettings settings = this.plugin.getConfigManager().getWorldSettings(worldName);

        // Always clean the world using either specific settings or defaults
        this.plugin.getWorldCleanerService().cleanWorld(player.getWorld(), settings);

        // Teleport player to spawn
        player.teleport(spawnLocation);

        // Send message if enabled
        if (this.plugin.getConfigManager().isMessageEnabled()) {
            player.sendMessage(this.plugin.getConfigManager().getSpawnMessage());
        }
    }
}