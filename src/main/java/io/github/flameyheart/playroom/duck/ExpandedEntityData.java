package io.github.flameyheart.playroom.duck;

public interface ExpandedEntityData {

    int playroom$getGunFreezeTicks();
    void playroom$setGunFreezeTicks(int frozenTicks);

    default boolean playroom$isFrozen() {
        return playroom$getGunFreezeTicks() > 0;
    }

    default boolean playroom$showIce() {
        return playroom$getGunFreezeTicks() > playroom$slowdownTime();
    }

    default float playroom$iceMeltProgress() {
        return Math.max((playroom$getGunFreezeTicks() - playroom$slowdownTime()) / (float) playroom$iceTime(), 0);
    }

    default boolean playroom$isSlowedDown() {
        return playroom$getGunFreezeTicks() > 0 && playroom$slowdownTime() >= playroom$getGunFreezeTicks();
    }

    default boolean playroom$isFrozenDelayed() {
        return playroom$getGunFreezeTicks() > 0 && playroom$getGunFreezeTicks() < playroom$freezeTime();
    }

    default int playroom$slowdownTime() {
        return 50;
    }

    default int playroom$iceTime() {
        return 100;
    }

    default int playroom$zoomStart() {
        return playroom$freezeTime() - 20;
    }

    default int playroom$freezeTime() {
        return playroom$iceTime() + playroom$slowdownTime();
    }
}
