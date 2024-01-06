package io.github.flameyheart.playroom.mixin;

import io.github.flameyheart.playroom.duck.FreezableEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "updateInput", at = @At("HEAD"), cancellable = true)
    private void ignoreInputUpdates(float sidewaysSpeed, float forwardSpeed, boolean jumping, boolean sneaking, CallbackInfo ci) {
        if (((FreezableEntity) this).playroom$isFrozen()) {
            ci.cancel();
        }
    }
}
