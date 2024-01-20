package io.github.flameyheart.playroom.dontation;

public enum DonationLocation {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_RIGHT,
    CHAT;

    public String translationKey() {
        return "playroom.donation_location." + name().toLowerCase();
    }
}
