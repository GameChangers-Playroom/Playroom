package io.github.flameyheart.playroom.tiltify.action.trulyrandom;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ResetableTrulyRandomAction extends TrulyRandomAction {
    default Untargeted onResetUntargeted() {
        return Untargeted.EMPTY;
    }

    default Targeted onResetTargeted() {
        return Targeted.EMPTY;
    }

    int getTicksUntilReset();

    interface Targeted {
        Targeted EMPTY = (target) -> {};

        void execute(ServerPlayerEntity target);
    }

    interface Untargeted {
        Untargeted EMPTY = (server) -> {};

        void execute(MinecraftServer server);
    }
}
