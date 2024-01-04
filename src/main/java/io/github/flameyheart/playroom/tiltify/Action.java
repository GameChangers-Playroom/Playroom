package io.github.flameyheart.playroom.tiltify;

import net.minecraft.server.network.ServerPlayerEntity;

public interface Action {
    void execute(ServerPlayerEntity target);
}
