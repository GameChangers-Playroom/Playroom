package io.github.flameyheart.playroom.duck;

public interface SnowOverlay {
    void playroom$setOverlayTime(int ticks);
    default void playroom$addOverlayTime(int ticks) {
        playroom$setOverlayTime(playroom$getOverlayTime() + ticks);
    }
    int playroom$getOverlayTime();
    float playroom$getOverlayProgress();
}
