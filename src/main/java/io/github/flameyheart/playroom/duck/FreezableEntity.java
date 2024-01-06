package io.github.flameyheart.playroom.duck;

import io.github.flameyheart.playroom.config.ServerConfig;

public interface FreezableEntity {
    void playroom$setSlowdownTime(int ticks);
    default void playroom$addSlowdownTime(int ticks) {
        int finalTime = playroom$getSlowdownTime() + ticks;
        if (finalTime > playroom$maxSlowdownTime()) {
            playroom$freeze();
        } else {
            playroom$setSlowdownTime(finalTime);
        }
    }
    int playroom$getSlowdownTime();
    default boolean playroom$isSlowed() {
        return playroom$getSlowdownTime() > 0;
    }
    default int playroom$maxSlowdownTime() {
        return ServerConfig.instance().freezeSlowdownTime;
    }

    void playroom$setFreezeTime(int ticks);
    default void playroom$addFreezeTime(int ticks) {
        playroom$setFreezeTime(playroom$getFreezeTime() + ticks);
    }
    int playroom$getFreezeTime();
    default boolean playroom$isFrozen() {
        return playroom$getFreezeTime() > 0;
    }
    default int playroom$maxFreezeTime() {
        return ServerConfig.instance().freezeIceTime;
    }
    default float playroom$getMeltProgress() {
        return Math.max(playroom$getFreezeTime() / (float) playroom$maxFreezeTime(), 0);
    }

    default boolean playroom$isAffected() {
        return playroom$isSlowed() || playroom$isFrozen();
    }
    default void playroom$addTime(int ticks) {
        if (playroom$isFrozen()) {
            playroom$addFreezeTime(ticks);
        } else if (playroom$isSlowed()) {
            playroom$addSlowdownTime(ticks);
        }
    }

    default void playroom$freeze() {
        playroom$setFreezeTime(playroom$maxFreezeTime());
        playroom$setSlowdownTime(0);
    }
    default void playroom$slowdown() {
        playroom$setSlowdownTime(playroom$maxSlowdownTime());
    }

    /**
     * Ensure to call {@link #playroom$tickFreezeLogic()} inside implementations<br>
     * Cannot call FreezableEntity.super.playroom$tick() because of a bug in mixin
     */
    void playroom$tick();

    /**
     * Call inside overrides for {@link #playroom$tick()}
     */
    default void playroom$tickFreezeLogic() {
        if (playroom$isFrozen()) {
            playroom$addFreezeTime(-1);
        } else if (playroom$isSlowed()) {
            playroom$addSlowdownTime(-1);
        }
    }
}
