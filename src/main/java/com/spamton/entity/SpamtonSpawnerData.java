package com.spamton.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** In-memory storage for Spamton merchant glasses UUIDs (1.21.11 entity persistence uses ValueInput/ValueOutput). */
public final class SpamtonSpawnerData {
    private static final Map<UUID, UUID> GLASSES_LEFT = new HashMap<>();
    private static final Map<UUID, UUID> GLASSES_RIGHT = new HashMap<>();

    private SpamtonSpawnerData() {}

    public static void setGlasses(UUID villagerId, UUID leftId, UUID rightId) {
        GLASSES_LEFT.put(villagerId, leftId);
        GLASSES_RIGHT.put(villagerId, rightId);
    }

    public static UUID getGlassesLeft(UUID villagerId) {
        return GLASSES_LEFT.get(villagerId);
    }

    public static UUID getGlassesRight(UUID villagerId) {
        return GLASSES_RIGHT.get(villagerId);
    }

    public static void clearGlasses(UUID villagerId) {
        GLASSES_LEFT.remove(villagerId);
        GLASSES_RIGHT.remove(villagerId);
    }
}
