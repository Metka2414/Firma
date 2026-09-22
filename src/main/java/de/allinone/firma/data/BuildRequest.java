package de.allinone.firma.data;

import java.util.UUID;

public class BuildRequest {

    private final UUID id;
    private final UUID requester;
    private final String description;
    private final double offer;
    private boolean accepted;
    private boolean completed;

    public BuildRequest(UUID requester, String description, double offer) {
        this.id = UUID.randomUUID();
        this.requester = requester;
        this.description = description;
        this.offer = offer;
        this.accepted = false;
        this.completed = false;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRequester() {
        return requester;
    }

    public String getDescription() {
        return description;
    }

    public double getOffer() {
        return offer;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
