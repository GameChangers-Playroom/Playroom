package io.github.flameyheart.playroom.mixin;

import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.event.LivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void onTickStart(CallbackInfo ci) {
        LivingEntityEvents.START_TICK.invoker().onStartTick((LivingEntity) (Object) this);
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void onTickEnd(CallbackInfo ci) {
        LivingEntityEvents.END_TICK.invoker().onEndTick((LivingEntity) (Object) this);
    }

    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", shift = At.Shift.AFTER))
    private void onBaseTickStart(CallbackInfo ci) {
        LivingEntityEvents.START_BASE_TICK.invoker().onStartTick((LivingEntity) (Object) this);
    }

    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", shift = At.Shift.BEFORE))
    private void onBaseTickEnd(CallbackInfo ci) {
        LivingEntityEvents.END_BASE_TICK.invoker().onEndTick((LivingEntity) (Object) this);
    }

    @Inject(method = "travel", at = @At(value = "HEAD"))
    private void onTravelStart(CallbackInfo ci) {
        if (isLogicalSideForUpdatingMovement()) {
            LivingEntityEvents.START_TRAVEL.invoker().onStartTravel((LivingEntity) (Object) this);
        }
    }

    @Inject(method = "travel", at = @At(value = "TAIL"))
    private void onTravelEnd(CallbackInfo ci) {
        if (isLogicalSideForUpdatingMovement()) {
            LivingEntityEvents.END_TRAVEL.invoker().onEndTravel((LivingEntity) (Object) this);
        }
    }

    @ModifyVariable(method = "travel",
      slice = @Slice(
        from = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getSlipperiness()F"),
        to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyMovementInput(Lnet/minecraft/util/math/Vec3d;F)Lnet/minecraft/util/math/Vec3d;")
      ),
      at = @At(value = "STORE"),
      ordinal = 0
    )
    private float iceDrag(float original) {
        if (((ExpandedEntityData) this).playroom$isFrozen()) {
            return this.isOnGround() ? 0.98f : 0.01f;
        }
        return original;
    }

    @Inject(method = "getOffGroundSpeed", at = @At(value = "HEAD"), cancellable = true)
    private void addIceDrag(CallbackInfoReturnable<Float> cir) {
        if (((ExpandedEntityData) this).playroom$isFrozen()) {
            cir.setReturnValue(0.01f);
        }
    }
}
