package io.github.flameyheart.playroom.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.duck.InventoryDuck;
import io.github.flameyheart.playroom.item.LaserGun;
import io.github.flameyheart.playroom.registry.Items;
import io.github.flameyheart.playroom.util.InventorySlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin {
    @Shadow private ItemStack selectedItem;

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

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAttributeValue(Lnet/minecraft/entity/attribute/EntityAttribute;)D"))
    private double slowdown(double original) {
        if (this.playroom$isSlowedDown()) {
            return original * ServerConfig.instance().freezeSlowdown;
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
