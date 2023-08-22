package io.github.flameyheart.playroom.mixin;

import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class PlayroomEntity implements ExpandedEntityData {
    @Shadow @Final protected DataTracker dataTracker;
    @Shadow public abstract boolean hasVehicle();
    @Shadow public abstract void stopRiding();

    private static final @Unique TrackedData<Integer> playroom$GUN_FREEZE_TICKS = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.INTEGER);

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;initDataTracker()V", shift = At.Shift.BEFORE))
    private void initTrackers(EntityType<?> type, World world, CallbackInfo ci) {
        this.dataTracker.startTracking(playroom$GUN_FREEZE_TICKS, 0);
    }

    @Inject(method = "writeNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
    private void appendNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        NbtCompound compound = new NbtCompound();
        compound.putInt("TicksFrozen", playroom$getGunFreezeTicks());

        nbt.put("Playroom", compound);
    }

    @Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
    private void appendNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound compound = nbt.getCompound("Playroom");
        playroom$setGunFreezeTicks(compound.getInt("TicksFrozen"));
    }

    @Override
    public int playroom$getGunFreezeTicks() {
        return this.dataTracker.get(playroom$GUN_FREEZE_TICKS);
    }

    @Override
    public void playroom$setGunFreezeTicks(int frozenTicks) {
        this.dataTracker.set(playroom$GUN_FREEZE_TICKS, frozenTicks);
        if (hasVehicle()) stopRiding();
    }
}
