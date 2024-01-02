package io.github.flameyheart.playroom.duck;

import io.github.flameyheart.playroom.config.ServerConfig;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

public interface ExpandedEntityData {
    void playroom$setAiming(boolean aiming);
    boolean playroom$isAiming();
    void playroom$setDisplayName(Text prefix, Text displayName);
    Pair<Text, Text> playroom$getDisplayName();

    int playroom$getGunFreezeTicks();
    void playroom$setGunFreezeTicks(int frozenTicks);
    void playroom$addGunFreezeTicks(int frozenTicks);

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
        return (playroom$getGunFreezeTicks() > 0 && playroom$slowdownTime() >= playroom$getGunFreezeTicks());
    }

    default int playroom$slowdownTime() {
        return ServerConfig.instance().freezeSlowdownTime;
    }

    default int playroom$iceTime() {
        return ServerConfig.instance().freezeIceTime;
    }

    default int playroom$zoomStart() {
        return playroom$slowdownTime() + playroom$zoomDuration();
    }

    default boolean playroom$showZoom() {
        return playroom$getGunFreezeTicks() > playroom$slowdownTime() && playroom$getGunFreezeTicks() <= playroom$zoomStart() - ServerConfig.instance().freezeZoomOffset;
    }

    default boolean playroom$showZoomOffset() {
        int offset = ServerConfig.instance().freezeZoomOffset;
        return playroom$getGunFreezeTicks() > playroom$slowdownTime() - offset && playroom$getGunFreezeTicks() <= playroom$zoomStart() - offset;
    }

    default int playroom$zoomDuration() {
        return ServerConfig.instance().freezeZoomDuration;
    }

    default int playroom$freezeTime() {
        return playroom$iceTime() + playroom$slowdownTime();
    }

    default void playroom$freeze() {
        playroom$setGunFreezeTicks(playroom$freezeTime());
    }
}
