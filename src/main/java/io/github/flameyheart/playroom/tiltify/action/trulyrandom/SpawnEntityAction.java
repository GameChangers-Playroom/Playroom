package io.github.flameyheart.playroom.tiltify.action.trulyrandom;

import io.github.flameyheart.playroom.tiltify.Action;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class SpawnEntityAction implements Action<Entity> {
    @Override
    public boolean execute(ServerPlayerEntity target, Entity entity, UUID id) {
        if (entity == null) return false;
        if (entity instanceof MobEntity mob) {
            mob.initialize(target.getServerWorld(), target.getServerWorld().getLocalDifficulty(mob.getBlockPos()), SpawnReason.TRIGGERED, null, null);
            mob.setPersistent();
        }
        target.getServerWorld().spawnEntityAndPassengers(entity);
        return true;
    }
}
