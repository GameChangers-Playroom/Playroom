package io.github.flameyheart.playroom.mixin;

import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
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

    @Shadow private World world;
    private static final @Unique TrackedData<Integer> playroom$GUN_FREEZE_TICKS = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.INTEGER);
    private @Unique Text playroom$prefix;
    private @Unique Text playroom$displayName;

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
        if (world.isClient()) return;
        this.dataTracker.set(playroom$GUN_FREEZE_TICKS, frozenTicks);
        if (hasVehicle()) stopRiding();
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
    public Pair<Text, Text> playroom$getDisplayName() {
        return new Pair<>(playroom$prefix, playroom$displayName);
    }
}
