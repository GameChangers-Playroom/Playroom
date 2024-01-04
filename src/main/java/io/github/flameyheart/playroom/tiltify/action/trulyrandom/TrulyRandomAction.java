package io.github.flameyheart.playroom.tiltify.action.trulyrandom;

import com.bawnorton.trulyrandom.TrulyRandom;
import com.bawnorton.trulyrandom.random.ServerRandomiser;
import com.bawnorton.trulyrandom.random.module.Module;
import io.github.flameyheart.playroom.tiltify.Action;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Actions that are executed by TrulyRandomApi. Provide the executor as a method reference, either targeted (player)
 * or untargeted (server).
 * When running an untargeted action, pass any player as the target (doesn't matter, just gets the server).
 */
public interface TrulyRandomAction extends Action {
    Module getModule();

    boolean isTargeted();

    default Targeted getTargeted() {
        return Targeted.EMPTY;
    }

    default Untargeted getUntargeted() {
        return Untargeted.EMPTY;
    }

    default void execute(ServerPlayerEntity target) {
        MinecraftServer server = target.getServer();
        if (server == null) return;

        ServerRandomiser randomiser = TrulyRandom.getRandomiser(server);
        if(randomiser.getModules().isEnabled(getModule())) {
            if (isTargeted()) {
                getTargeted().execute(target, true);
            } else {
                getUntargeted().execute(server, true);
            }
        }
    }

    interface Targeted {
        Targeted EMPTY = (target, randomSeed) -> {};

        void execute(ServerPlayerEntity target, boolean randomSeed);
    }

    interface Untargeted {
        Untargeted EMPTY = (server, randomSeed) -> {};

        void execute(MinecraftServer server, boolean randomSeed);
    }
}
