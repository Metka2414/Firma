package de.allinone.firma.gui;

import de.allinone.firma.data.FirmaType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FirmaCreationSession {

    private final UUID playerId;
    private final FirmaType type;

    public FirmaCreationSession(UUID playerId, FirmaType type) {
        this.playerId = playerId;
        this.type = type;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public FirmaType getType() {
        return type;
    }

    private static final Map<UUID, FirmaCreationSession> ACTIVE = new HashMap<>();

    public static void start(UUID playerId, FirmaType type) {
        ACTIVE.put(playerId, new FirmaCreationSession(playerId, type));
    }

    public static FirmaCreationSession get(UUID playerId) {
        return ACTIVE.get(playerId);
    }

    public static void clear(UUID playerId) {
        ACTIVE.remove(playerId);
    }

    public static boolean isActive(UUID playerId) {
        return ACTIVE.containsKey(playerId);
    }
}
