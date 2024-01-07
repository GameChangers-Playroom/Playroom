package io.github.flameyheart.playroom.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.duck.AimingEntity;
import io.github.flameyheart.playroom.duck.InventoryDuck;
import io.github.flameyheart.playroom.item.LaserGun;
import io.github.flameyheart.playroom.registry.Items;
import io.github.flameyheart.playroom.util.InventorySlot;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin implements AimingEntity {
    @Shadow private ItemStack selectedItem;
    @Shadow public abstract void stopFallFlying();
    @Shadow protected abstract void dropShoulderEntities();

    private static final @Unique TrackedData<Boolean> playroom$IS_AIMING = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    @Inject(method = "initDataTracker", at = @At("HEAD"))
    private void trackGunFreezeTicks(CallbackInfo ci) {
        this.dataTracker.startTracking(playroom$IS_AIMING, false);
    }

    public void playroom$setAiming(boolean aiming) {
        this.dataTracker.set(playroom$IS_AIMING, aiming);
    }
    public boolean playroom$isAiming() {
        return this.dataTracker.get(playroom$IS_AIMING);
    }

    @Override
    public void playroom$tick() {
        super.playroom$tick();
        if (!this.playroom$isAffected()) return;
        if (this.isFallFlying()) this.stopFallFlying();
        dropShoulderEntities();
    }

    @Inject(method = "addShoulderEntity", at = @At(value = "HEAD"), cancellable = true)
    private void preventShoulderEntities(NbtCompound entityNbt, CallbackInfoReturnable<Boolean> cir) {
        if (this.playroom$isAffected()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getOffGroundSpeed", at = @At(value = "HEAD"), cancellable = true)
    private void airResistance(CallbackInfoReturnable<Float> cir) {
        if (this.playroom$isFrozen()) {
            cir.setReturnValue(0.01f);
        }
    }

    @ModifyVariable(method = "handleFallDamage", at = @At(value = "HEAD"), index = 2, argsOnly = true)
    private float reduceIceTimeFromFall(float damageMultiplier, float fallDistance) {
        if (this.playroom$isFrozen()) {
            this.playroom$addFreezeTime(-(int) Math.ceil(fallDistance * ServerConfig.instance().freezeFallIceDamage));
            return damageMultiplier * (1 - ServerConfig.instance().freezeFallDamageReduction);
        }
        return damageMultiplier;
    }

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private boolean disableRegen(boolean original) {
        if (this.playroom$isFrozen() || this.selectedItem.getItem() instanceof LaserGun) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAttributeValue(Lnet/minecraft/entity/attribute/EntityAttribute;)D"))
    private double aimSlowdown0(double original) {
        if (this.playroom$isAiming()) {
            return original * ServerConfig.instance().laserAimSlowdown;
        }
        return original;
    }

    @ModifyExpressionValue(method = "getMovementSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAttributeValue(Lnet/minecraft/entity/attribute/EntityAttribute;)D"))
    private double aimSlowdown1(double original) {
        if (this.playroom$isAiming()) {
            return original * ServerConfig.instance().laserAimSlowdown;
        }
        return original;
    }

    @Inject(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;dropAll()V"))
    private void keepLaserGun(CallbackInfo callbackInfo) {
        //noinspection ConstantConditions
        if ((Object) this instanceof ServerPlayerEntity serverPlayer) {
            PlayerInventory inventory = serverPlayer.getInventory();
            InventoryDuck duck = (InventoryDuck) inventory;
            if (inventory.count(Items.LASER_GUN) > 0) {
                InventorySlot slot = duck.playroom$removeFirstStack(Items.LASER_GUN);
                if (slot == null) return;
                Playroom.GUN_STACKS.put(serverPlayer.getUuid(), slot);
            }
        }
    }
}
