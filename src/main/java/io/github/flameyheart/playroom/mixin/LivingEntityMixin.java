package io.github.flameyheart.playroom.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.duck.FreezableEntity;
import io.github.flameyheart.playroom.duck.FreezeOverlay;
import io.github.flameyheart.playroom.entity.attribute.DynamicEntityAttributeModifier;
import io.github.flameyheart.playroom.event.LivingEntityEvents;
import io.github.flameyheart.playroom.registry.Tags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends PlayroomEntity implements FreezableEntity, FreezeOverlay {
    @Shadow
    public abstract void stopRiding();

    @Shadow
    public abstract boolean isFallFlying();

    @Shadow
    public abstract boolean isDead();

    @Shadow
    public abstract @Nullable EntityAttributeInstance getAttributeInstance(EntityAttribute attribute);

    private static final @Unique TrackedData<Integer> playroom$FREEZE = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final @Unique TrackedData<Integer> playroom$SLOWDOWN = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final @Unique TrackedData<Integer> playroom$SNOW_OVERLAY = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final @Unique UUID playroom$SLOWDOWN_ID = UUID.fromString("3bceceb5-bdd6-43b5-a8f6-64e0eb1893b1");


    @Inject(method = "initDataTracker", at = @At("HEAD"))
    private void trackGunFreezeTicks(CallbackInfo ci) {
        this.dataTracker.startTracking(playroom$FREEZE, 0);
        this.dataTracker.startTracking(playroom$SLOWDOWN, 0);
        this.dataTracker.startTracking(playroom$SNOW_OVERLAY, 0);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        NbtCompound compound = new NbtCompound();
        compound.putInt("Freeze", playroom$getFreezeTime());
        compound.putInt("Slowdown", playroom$getSlowdownTime());

        nbt.put("Playroom", compound);
    }

    @Override
    protected void readNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound compound = nbt.getCompound("Playroom");
        playroom$setFreezeTime(compound.getInt("Freeze"));
        playroom$setSlowdownTime(compound.getInt("Slowdown"));
    }

    @Override
    protected boolean isCollidable(boolean original) {
        return super.isCollidable(original) || this.playroom$isFrozen();
    }

    @Override
    protected boolean canMoveVoluntarily(boolean original) {
        if (this.playroom$isFrozen() && !((Object) this instanceof PlayerEntity)) {
            return false;
        }
        return super.canMoveVoluntarily(original);
    }

    @Override
    public void playroom$setSlowdownTime(int ticks) {
        int time = MathHelper.clamp(ticks, 0, playroom$maxSlowdownTime());
        this.dataTracker.set(playroom$SLOWDOWN, time);

        EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (entityAttributeInstance == null || playroom$maxSlowdownTime() <= 0) return;
        if (entityAttributeInstance.getModifier(playroom$SLOWDOWN_ID) != null && time == 0) {
            entityAttributeInstance.removeModifier(playroom$SLOWDOWN_ID);
        } else if (entityAttributeInstance.getModifier(playroom$SLOWDOWN_ID) == null && time > 0) {
            entityAttributeInstance.addTemporaryModifier(
              new DynamicEntityAttributeModifier(
                playroom$SLOWDOWN_ID,
                "Aim slowdown",
                () -> -ServerConfig.instance().freezeSlowdown * ((double) playroom$getSlowdownTime() / playroom$maxSlowdownTime()),
                EntityAttributeModifier.Operation.MULTIPLY_BASE
              )
            );
        }
    }

    @Override
    public int playroom$getSlowdownTime() {
        return this.dataTracker.get(playroom$SLOWDOWN);
    }

    @Override
    public void playroom$setFreezeTime(int ticks) {
        this.dataTracker.set(playroom$FREEZE, MathHelper.clamp(ticks, 0, playroom$maxFreezeTime()));
        if (hasVehicle()) stopRiding();
        if (ticks > 0) {
            this.playroom$setOverlayTime(15);
        }
    }

    @Override
    public int playroom$getFreezeTime() {
        return this.dataTracker.get(playroom$FREEZE);
    }

    /**
     * Use the same code as in FreezableEntity to get around a bug in mixin
     *
     * @see <a href="https://github.com/SpongePowered/Mixin/issues/504">Issue 504</a>
     **/
    @Override
    public void playroom$tick() {
        if (this.getWorld().isClient || this.isDead()) return;
        if (playroom$isFrozen()) {
            playroom$addFreezeTime(-1);
        } else if (playroom$isSlowed()) {
            playroom$addSlowdownTime(-1);
        }
        if (this.playroom$isAffected()) {
            if (this.hasPassengers()) this.removeAllPassengers();
            if (this.isOnFire()) {
                int fireTicks = this.getFireTicks();
                int timeLeft = Math.max(this.playroom$getFreezeTime() - fireTicks, 0);
                this.playroom$addTime(-fireTicks);
                this.setFireTicks(timeLeft);
            }
        }
        if (!playroom$isAffected() && playroom$getOverlayTime() > 0) {
            playroom$addOverlayTime(-1);
        }
    }

    @Override
    public void playroom$setOverlayTime(int ticks) {
        this.dataTracker.set(playroom$SNOW_OVERLAY, ticks);
    }

    @Override
    public int playroom$getOverlayTime() {
        return playroom$isSlowed() ? playroom$getSlowdownTime() : this.dataTracker.get(playroom$SNOW_OVERLAY);
    }

    @Override
    public float playroom$getOverlayProgress() {
        return playroom$isFrozen() ? 1 : playroom$getOverlayTime() / (float) playroom$getMaxOverlayTime();
    }

    @Override
    public int playroom$getMaxOverlayTime() {
        return playroom$isSlowed() ? playroom$maxSlowdownTime() : 15;
    }

    @WrapWithCondition(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(DDD)V"))
    private boolean preventDelayedKnockback(LivingEntity instance, double strength, double x, double z, DamageSource source) {
        return !source.isIn(DamageTypeTags.NO_IMPACT);
    }

    @WrapWithCondition(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tiltScreen(DD)V"))
    private boolean preventDamageTilt(LivingEntity instance, double deltaX, double deltaZ, DamageSource source) {
        return !source.isIn(Tags.NO_TILT);
    }

    @WrapWithCondition(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;playHurtSound(Lnet/minecraft/entity/damage/DamageSource;)V"))
    private boolean skipHurtSound(LivingEntity instance, DamageSource source) {
        return !source.isIn(Tags.NO_HURT_SOUND);
    }

    @ModifyReturnValue(method = "isImmobile", at = @At("RETURN"))
    private boolean immobileIfFrozen(boolean original) {
        return original || playroom$isFrozen();
    }

    @ModifyReturnValue(method = "isUsingItem", at = @At("RETURN"))
    private boolean preventUsingItemsFrozen(boolean original) {
        return original && !playroom$isFrozen();
    }

    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void onTickMovementStart(CallbackInfo ci) {
        //if (playroom$isFrozen()) ci.cancel();
    }

    @Inject(method = "tick", at = @At(value = "HEAD"), cancellable = true)
    private void onTickStart(CallbackInfo ci) {
        if (LivingEntityEvents.START_TICK.invoker().onStartTick((LivingEntity) (Object) this)) ci.cancel();
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void onTickEnd(CallbackInfo ci) {
        LivingEntityEvents.END_TICK.invoker().onEndTick((LivingEntity) (Object) this);
    }

    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onBaseTickStart(CallbackInfo ci) {
        if (LivingEntityEvents.START_BASE_TICK.invoker().onStartTick((LivingEntity) (Object) this)) ci.cancel();
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
    private float slipperiness(float original) {
        if (this.playroom$isFrozen()) {
            return isOnGround() ? 0.98f : 0.01f;
        }
        return original;
    }

    @Inject(method = "getOffGroundSpeed", at = @At(value = "HEAD"), cancellable = true)
    private void airResistance(CallbackInfoReturnable<Float> cir) {
        if (this.playroom$isFrozen()) {
            cir.setReturnValue(0.01f);
        }
    }
}
