package io.github.flameyheart.playroom.duck;

public interface AimingEntity {
    void playroom$setAiming(boolean aiming);
    default boolean playroom$isAiming() {
        return false;
    }
}
