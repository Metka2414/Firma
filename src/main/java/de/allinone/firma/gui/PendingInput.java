package de.allinone.firma.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PendingInput {

    public enum Type {
        BANK_DEPOSIT,
        BANK_WITHDRAW,
        INVITE_PLAYER,
        SET_SALE_PRICE,
        RENAME_FIRMA,
        CREATE_PROPERTY_NAME,
        CREATE_PROPERTY_PRICE,
        CREATE_BUILD_OFFER_DESC
    }

    private final UUID playerId;
    private final Type type;
    private final UUID firmaId;
    private String tempData;

    public PendingInput(UUID playerId, Type type, UUID firmaId) {
        this.playerId = playerId;
        this.type = type;
        this.firmaId = firmaId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Type getType() {
        return type;
    }

    public UUID getFirmaId() {
        return firmaId;
    }

    public String getTempData() {
        return tempData;
    }

    public void setTempData(String tempData) {
        this.tempData = tempData;
    }

    private static final Map<UUID, PendingInput> ACTIVE = new HashMap<>();

    public static void start(UUID playerId, Type type, UUID firmaId) {
        ACTIVE.put(playerId, new PendingInput(playerId, type, firmaId));
    }

    public static PendingInput get(UUID playerId) {
        return ACTIVE.get(playerId);
    }

    public static void clear(UUID playerId) {
        ACTIVE.remove(playerId);
    }

    public static boolean isActive(UUID playerId) {
        return ACTIVE.containsKey(playerId);
    }
}
