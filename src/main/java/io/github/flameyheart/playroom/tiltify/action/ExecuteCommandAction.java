package io.github.flameyheart.playroom.tiltify.action;

import io.github.flameyheart.playroom.tiltify.Action;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class ExecuteCommandAction implements Action<String> {
    @Override
    public boolean execute(ServerPlayerEntity target, String command, UUID id) {
        return false;
    }
}
