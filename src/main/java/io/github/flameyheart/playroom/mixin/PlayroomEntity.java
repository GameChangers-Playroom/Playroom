package io.github.flameyheart.playroom.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
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

    @ModifyReturnValue(method = "isCollidable", at = @At("RETURN"))
    protected boolean collideWhileFrozen(boolean original) {
        return original;
    }

    @ModifyReturnValue(method = "canMoveVoluntarily", at = @At("RETURN"))
    protected boolean moveWhileFrozen(boolean original) {
        return original;
    }

    @Inject(method = "writeNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
    protected void appendNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        // no-op
    }

    @Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
    protected void appendNbt(NbtCompound nbt, CallbackInfo ci) {
        // no-op
    }
}
