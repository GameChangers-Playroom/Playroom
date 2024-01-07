package io.github.flameyheart.playroom.tiltify.action.trulyrandom;

public interface ServerResetableTrulyRandomAction extends ResetableTrulyRandomAction {
    @Override
    default boolean requiresPlayer() {
        return false;
    }
}
