package io.github.flameyheart.playroom.tiltify.action.trulyrandom;

import com.bawnorton.trulyrandom.TrulyRandom;
import com.bawnorton.trulyrandom.random.ServerRandomiser;
import com.bawnorton.trulyrandom.random.module.Module;
import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.tiltify.Action;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Actions that are executed by TrulyRandomApi. Provide the executor as a method reference, either targeted (player)
 * or untargeted (server).
 * When running an untargeted action, pass any player as the target (doesn't matter, just gets the server).
 */
public interface TrulyRandomAction extends Action<@Nullable Object> {
    Module getModule();

    default Targeted getTargeted() {
        return Targeted.EMPTY;
    }

    default Untargeted getUntargeted() {
        return Untargeted.EMPTY;
    }

    default boolean execute(ServerPlayerEntity target, @Nullable Object data) {
        Playroom.LOGGER.info("Executing action " + getModule().name() + " for " + (target == null ? "everyone" : target.getName().getString()));
        MinecraftServer server = Playroom.getServer();
        if (server == null) return false;

        ServerRandomiser randomiser = TrulyRandom.getRandomiser(server);
        if (requiresPlayer()) {
            getTargeted().execute(target, true);
            return true;
        } else {
            getUntargeted().execute(server, true);
            return randomiser.getModules().isEnabled(getModule());
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
