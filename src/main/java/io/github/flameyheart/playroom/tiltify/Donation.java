package io.github.flameyheart.playroom.tiltify;

import java.util.UUID;

public class Donation {
    private final UUID id;
    private final String donorName;
    private final String message;
    private final float amount;
    private final String currency;
    private Status status;

    public Donation(UUID id, String donorName, String message, float amount, String currency, boolean autoApprove) {
        this.id = id;
        this.donorName = donorName;
        this.message = message;
        this.amount = amount;
        this.currency = currency;
        this.status = autoApprove ? Status.AUTO_APPROVED : Status.NO_ACTION;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    public UUID id() {
        return this.id;
    }

    public String donorName() {
        return this.donorName;
    }

    public String message() {
        return this.message;
    }

    public float amount() {
        return this.amount;
    }

    public String currency() {
        return this.currency;
    }

    public Status status() {
        return this.status;
    }

    public enum Status {
        AUTO_APPROVED,
        MANUAL_APPROVED,
        NO_ACTION
    }
}
