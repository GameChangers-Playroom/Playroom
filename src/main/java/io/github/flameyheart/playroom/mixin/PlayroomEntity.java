package io.github.flameyheart.playroom.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class PlayroomEntity {
    @Shadow @Final protected DataTracker dataTracker;
    @Shadow public abstract boolean hasVehicle();
    @Shadow public abstract void stopRiding();
    @Shadow public abstract boolean isLogicalSideForUpdatingMovement();
    @Shadow public abstract boolean isOnGround();
    @Shadow public abstract World getWorld();
    @Shadow public abstract void removeAllPassengers();
    @Shadow public abstract boolean hasPassengers();
    @Shadow public abstract boolean isOnFire();
    @Shadow public abstract int getFireTicks();
    @Shadow public abstract void setFireTicks(int fireTicks);
    @Shadow protected abstract void playExtinguishSound();
    @Shadow public abstract double getX();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();

    @Shadow @Final protected Random random;

    @Shadow public abstract double offsetX(double widthScale);

    @Shadow public abstract double getRandomBodyY();

    @Shadow public abstract double getParticleZ(double widthScale);

    @Shadow public abstract double getParticleX(double widthScale);

    @ModifyReturnValue(method = "isCollidable", at = @At("RETURN"))
    protected boolean isCollidable(boolean original) {
        return original;
    }

    @ModifyReturnValue(method = "canMoveVoluntarily", at = @At("RETURN"))
    protected boolean canMoveVoluntarily(boolean original) {
        return original;
    }

    @Inject(method = "writeNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
    protected void writeNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        // no-op
    }

    @Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
    protected void readNbt(NbtCompound nbt, CallbackInfo ci) {
        // no-op
    }
}
