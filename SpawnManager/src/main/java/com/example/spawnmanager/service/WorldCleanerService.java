package com.example.spawnmanager.service;

import com.example.spawnmanager.SpawnManagerPlugin;
import com.example.spawnmanager.config.WorldCleaningSettings;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.World;
import org.bukkit.entity.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class WorldCleanerService {

    private final SpawnManagerPlugin plugin;
    private final MythicBukkit mythicMobs;

    public WorldCleanerService(final SpawnManagerPlugin plugin) {
        this.plugin = plugin;
        this.mythicMobs = MythicBukkit.inst();
    }

    public void cleanWorld(final World world, final WorldCleaningSettings settings) {
        if (world == null || settings == null) {
            return;
        }

        // Create weak reference to world
        WeakReference<World> worldRef = new WeakReference<>(world);

        // Get all entities first and store weak references
        List<WeakReference<Entity>> entityRefs = new ArrayList<>();
        for (Entity entity : world.getEntities()) {
            if (entity != null && !(entity instanceof Player)) {
                entityRefs.add(new WeakReference<>(entity));
            }
        }

        // Process entities using weak references
        for (WeakReference<Entity> entityRef : entityRefs) {
            Entity entity = entityRef.get();
            // Skip if entity has been unloaded/collected
            if (entity == null) {
                continue;
            }

            // Check if this entity should be removed based on settings
            boolean shouldRemove = false;

            // Check MythicMobs
            if (settings.isCleanMythicMobs() && this.mythicMobs.getMobManager().isActiveMob(entity.getUniqueId())) {
                this.mythicMobs.getMobManager().unregisterActiveMob(entity.getUniqueId());
                shouldRemove = true;
            }

            // Check items
            if (settings.isCleanItems() && entity instanceof Item) {
                shouldRemove = true;
            }

            // Check projectiles
            if (settings.isCleanProjectiles() && entity instanceof Projectile) {
                shouldRemove = true;
            }

            // Check armor stands (part of entities)
            if (settings.isCleanEntities() && (entity instanceof ArmorStand || entity instanceof Monster || entity instanceof Animals)) {
                shouldRemove = true;
            }

            // Check holograms (usually armor stands with specific properties)
            if (settings.isCleanHolograms() && entity instanceof ArmorStand) {
                ArmorStand stand = (ArmorStand) entity;
                if (stand.isInvisible() || stand.isMarker() || !stand.hasGravity()) {
                    shouldRemove = true;
                }
            }

            // Only remove if the settings allow it and the world is still loaded
            if (shouldRemove && worldRef.get() != null) {
                entity.remove();
            }
        }

        // Clear references to help GC
        entityRefs.clear();
        worldRef = null;
    }
}