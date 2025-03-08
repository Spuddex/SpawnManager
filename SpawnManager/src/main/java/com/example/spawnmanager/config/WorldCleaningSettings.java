package com.example.spawnmanager.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WorldCleaningSettings {
    private final boolean cleanMythicMobs;
    private final boolean cleanItems;
    private final boolean cleanEntities;
    private final boolean cleanHolograms;
    private final boolean cleanProjectiles;
}