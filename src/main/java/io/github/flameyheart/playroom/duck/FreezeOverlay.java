package io.github.flameyheart.playroom.duck;

public interface FreezeOverlay {
    void playroom$setOverlayTime(int ticks);
    default void playroom$addOverlayTime(int ticks) {
        playroom$setOverlayTime(playroom$getOverlayTime() + ticks);
    }
    int playroom$getOverlayTime();
    default int playroom$getMaxOverlayTime() {
        return 15;
    }
    float playroom$getOverlayProgress();
}
