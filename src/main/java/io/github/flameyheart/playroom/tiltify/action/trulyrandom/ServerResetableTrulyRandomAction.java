package io.github.flameyheart.playroom.tiltify.action.trulyrandom;

public interface ServerResetableTrulyRandomAction extends ResetableTrulyRandomAction {
    @Override
    default boolean isTargeted() {
        return false;
    }

    @Override
    default boolean requiresPlayer() {
        return false;
    }
}
