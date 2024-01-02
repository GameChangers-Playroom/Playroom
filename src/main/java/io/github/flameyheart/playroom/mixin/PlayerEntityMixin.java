package io.github.flameyheart.playroom.mixin;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.duck.InventoryDuck;
import io.github.flameyheart.playroom.item.LaserGun;
import io.github.flameyheart.playroom.registry.Items;
import io.github.flameyheart.playroom.util.InventorySlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin {
    private static final @Unique TrackedData<Boolean> playroom$IS_AIMING = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final @Unique UUID playroomAIM_SLOWDOWN_ID = UUID.fromString("3bceceb5-bdd6-43b5-a8f6-64e0eb1893b1");
    @Shadow private ItemStack selectedItem;

    @Inject(method = "initDataTracker", at = @At("HEAD"))
    private void trackGunFreezeTicks(CallbackInfo ci) {
        this.dataTracker.startTracking(playroom$IS_AIMING, false);
    }

    @Override
    public void playroom$setAiming(boolean aiming) {
        this.dataTracker.set(playroom$IS_AIMING, aiming);
    }

    @Override
    public boolean playroom$isAiming() {
        return this.dataTracker.get(playroom$IS_AIMING);
    }

    @Inject(method = "addShoulderEntity", at = @At(value = "HEAD"), cancellable = true)
    private void preventShoulderEntities(NbtCompound entityNbt, CallbackInfoReturnable<Boolean> cir) {
        if (this.playroom$isFrozen()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getOffGroundSpeed", at = @At(value = "HEAD"), cancellable = true)
    private void addIceDrag(CallbackInfoReturnable<Float> cir) {
        if (this.playroom$isFrozen()) {
            cir.setReturnValue(0.01f);
        }
    }

    @ModifyVariable(method = "handleFallDamage", at = @At(value = "HEAD"), index = 2, argsOnly = true)
    private float reduceIceTimeFromFall(float damageMultiplier, float fallDistance) {
        if (this.playroom$showIce()) {
            this.playroom$addGunFreezeTicks(-(int) Math.ceil(fallDistance * 3f)); //TODO: config
            return damageMultiplier * 0.3f; //TODO: config
        }
        return damageMultiplier;
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private boolean disableRegen(GameRules instance, GameRules.Key<GameRules.BooleanRule> rule) {
        if (this.playroom$isFrozen() || this.selectedItem.getItem() instanceof LaserGun) {
            return false;
        }
        return instance.getBoolean(rule);
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAttributeValue(Lnet/minecraft/entity/attribute/EntityAttribute;)D", shift = At.Shift.BEFORE))
    private void slowdown(CallbackInfo ci) {
        EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (entityAttributeInstance == null) return;
        if (entityAttributeInstance.getModifier(playroomAIM_SLOWDOWN_ID) != null && !this.playroom$isAiming()) {
            entityAttributeInstance.removeModifier(playroomAIM_SLOWDOWN_ID);
        } else if (entityAttributeInstance.getModifier(playroomAIM_SLOWDOWN_ID) == null && this.playroom$isAiming()) {
            entityAttributeInstance.addTemporaryModifier(new EntityAttributeModifier(playroomAIM_SLOWDOWN_ID, "Aim slowdown", -ServerConfig.instance().laserAimSlowdown, EntityAttributeModifier.Operation.MULTIPLY_BASE));
        }
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
