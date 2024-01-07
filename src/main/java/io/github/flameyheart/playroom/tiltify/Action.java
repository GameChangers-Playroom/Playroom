package io.github.flameyheart.playroom.tiltify;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public interface Action<T> {
    /**
     * @param target The player that triggered the action. Is always null if requiresPlayer() returns false
     * @param data   The extra data for the action, cna be null. It is always null if the action doesn't require extra data
     * @return Whether the action was successful
     **/
    boolean execute(@Nullable ServerPlayerEntity target, @Nullable T data);
    default boolean requiresPlayer() {
        return true;
    }
}
