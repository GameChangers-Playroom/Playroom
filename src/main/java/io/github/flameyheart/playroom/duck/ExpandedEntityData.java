package io.github.flameyheart.playroom.duck;

public interface ExpandedEntityData {

    int playroom$getGunFreezeTicks();
    void playroom$setGunFreezeTicks(int frozenTicks);

    default boolean playroom$isFrozen() {
        return playroom$getGunFreezeTicks() > playroom$slowdownTime();
    }

    default boolean playroom$isSlowedDown() {
        return playroom$getGunFreezeTicks() > 0 && playroom$slowdownTime() >= playroom$getGunFreezeTicks();
    }

    default boolean playroom$isFrozenDelayed() {
        return playroom$getGunFreezeTicks() > 0 && playroom$getGunFreezeTicks() < playroom$maxFrozenTime();
    }

    default int playroom$slowdownTime() {
        return 50;
    }

    default int playroom$iceTime() {
        return 100;
    }

    default int playroom$zoomStart() {
        return 20;
    }

    default int playroom$maxFrozenTime() {
        return playroom$iceTime() + playroom$slowdownTime();
    }
}
