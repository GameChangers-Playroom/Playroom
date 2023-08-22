package io.github.flameyheart.playroom.mixin;

import io.github.flameyheart.playroom.event.EntityTickEvents;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", shift = At.Shift.AFTER))
    private void onBaseTickStart(CallbackInfo ci) {
        EntityTickEvents.START_BASE_TICK.invoker().onStartTick((Entity) (Object) this);
    }

    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", shift = At.Shift.BEFORE))
    private void onBaseTickEnd(CallbackInfo ci) {
        EntityTickEvents.END_BASE_TICK.invoker().onEndTick((Entity) (Object) this);
    }
}
