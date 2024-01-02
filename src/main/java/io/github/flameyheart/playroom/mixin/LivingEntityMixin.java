package io.github.flameyheart.playroom.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.event.LivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends PlayroomEntity implements ExpandedEntityData {
    @Shadow public int hurtTime;
    private static final @Unique TrackedData<Integer> playroom$GUN_FREEZE_TICKS = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private @Unique boolean playroom$aiming;
    private @Unique Text playroom$prefix;
    private @Unique Text playroom$displayName;

    @Inject(method = "initDataTracker", at = @At("HEAD"))
    private void trackGunFreezeTicks(CallbackInfo ci) {
        this.dataTracker.startTracking(playroom$GUN_FREEZE_TICKS, 0);
    }

    @Override
    public int playroom$getGunFreezeTicks() {
        return this.dataTracker.get(playroom$GUN_FREEZE_TICKS);
    }

    @Override
    public void playroom$setGunFreezeTicks(int frozenTicks) {
        if (getWorld().isClient()) return;
        this.dataTracker.set(playroom$GUN_FREEZE_TICKS, frozenTicks);
        if (hasVehicle()) stopRiding();
    }

    @Override
    protected void appendNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        NbtCompound compound = new NbtCompound();
        compound.putInt("TicksFrozen", playroom$getGunFreezeTicks());

        nbt.put("Playroom", compound);
    }

    @Override
    protected void appendNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound compound = nbt.getCompound("Playroom");
        playroom$setGunFreezeTicks(compound.getInt("TicksFrozen"));
    }

    @Override
    protected boolean collideWhileFrozen(boolean original) {
        return super.collideWhileFrozen(original) || playroom$showIce();
    }

    @Override
    protected boolean moveWhileFrozen(boolean original) {
        if(playroom$showIce() && !((Object) this instanceof PlayerEntity)) {
            return false;
        }
        return super.moveWhileFrozen(original);
    }

    @Override
    public void playroom$addGunFreezeTicks(int frozenTicks) {
        playroom$setGunFreezeTicks(MathHelper.clamp(playroom$getGunFreezeTicks() + frozenTicks, 0, playroom$freezeTime()));
    }

    @Override
    public void playroom$setDisplayName(Text prefix, Text displayName) {
        this.playroom$prefix = prefix;
        this.playroom$displayName = displayName;
    }

    @Override
    public void playroom$setAiming(boolean aiming) {
        // NO OP
    }

    @Override
    public boolean playroom$isAiming() {
        return false;
    }

    @Override
    public Pair<Text, Text> playroom$getDisplayName() {
        return new Pair<>(playroom$prefix, playroom$displayName);
    }

    @ModifyReturnValue(method = "isImmobile", at = @At("RETURN"))
    private boolean immobileIfFrozen(boolean original) {
        return original || playroom$showIce();
    }

    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void onTickMovementStart(CallbackInfo ci) {
        if(playroom$showIce() && hurtTime == 0) ci.cancel();
    }

    @Inject(method = "tick", at = @At(value = "HEAD"), cancellable = true)
    private void onTickStart(CallbackInfo ci) {
        if(LivingEntityEvents.START_TICK.invoker().onStartTick((LivingEntity) (Object) this)) ci.cancel();
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void onTickEnd(CallbackInfo ci) {
        LivingEntityEvents.END_TICK.invoker().onEndTick((LivingEntity) (Object) this);
    }

    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onBaseTickStart(CallbackInfo ci) {
        if(LivingEntityEvents.START_BASE_TICK.invoker().onStartTick((LivingEntity) (Object) this)) ci.cancel();
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
        if (this.playroom$isFrozen()) {
            return isOnGround() ? 0.98f : 0.01f;
        }
        return original;
    }

    @Inject(method = "getOffGroundSpeed", at = @At(value = "HEAD"), cancellable = true)
    private void addIceDrag(CallbackInfoReturnable<Float> cir) {
        if (this.playroom$isFrozen()) {
            cir.setReturnValue(0.01f);
        }
    }
}
