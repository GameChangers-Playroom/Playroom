package io.github.flameyheart.playroom.tiltify.action.trulyrandom;

import io.github.flameyheart.playroom.Playroom;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ResetableTrulyRandomAction extends TrulyRandomAction {
    @Override
    default boolean execute(ServerPlayerEntity target, @Nullable Object data, UUID id) {
        Playroom.schedule(() -> {
            if (requiresPlayer()) {
                onResetTargeted().execute(target);
            } else {
                onResetUntargeted().execute(Playroom.getServer());
            }
        }, getDuration(), id);
        return TrulyRandomAction.super.execute(target, data, id);
    }

    default Untargeted onResetUntargeted() {
        return Untargeted.EMPTY;
    }

    default Targeted onResetTargeted() {
        return Targeted.EMPTY;
    }

    int getDuration();

    interface Targeted {
        Targeted EMPTY = (target) -> {};

        void execute(ServerPlayerEntity target);
    }

    interface Untargeted {
        Untargeted EMPTY = (server) -> {};

        void execute(MinecraftServer server);
    }
}
