package io.github.flameyheart.playroom.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.item.LaserGun;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow private ItemStack selectedItem;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "addShoulderEntity", at = @At(value = "HEAD"), cancellable = true)
    private void preventShoulderEntities(NbtCompound entityNbt, CallbackInfoReturnable<Boolean> cir) {
        if (((ExpandedEntityData) this).playroom$isFrozen()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getOffGroundSpeed", at = @At(value = "HEAD"), cancellable = true)
    private void addIceDrag(CallbackInfoReturnable<Float> cir) {
        if (((ExpandedEntityData) this).playroom$isFrozen()) {
            cir.setReturnValue(0.01f);
        }
    }

    @ModifyVariable(method = "handleFallDamage", at = @At(value = "HEAD"), index = 2, argsOnly = true)
    private float reduceIceTimeFromFall(float damageMultiplier, float fallDistance) {
        if (((ExpandedEntityData) this).playroom$showIce()) {
            ((ExpandedEntityData) this).playroom$addGunFreezeTicks(-(int) Math.ceil(fallDistance * 3f)); //TODO: config
            return damageMultiplier * 0.3f; //TODO: config
        }
        return damageMultiplier;
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private boolean disableRegen(GameRules instance, GameRules.Key<GameRules.BooleanRule> rule) {
        if (((ExpandedEntityData) this).playroom$isFrozen() || this.selectedItem.getItem() instanceof LaserGun) {
            return false;
        }
        return instance.getBoolean(rule);
    }

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAttributeValue(Lnet/minecraft/entity/attribute/EntityAttribute;)D"))
    private double slowdown(double original) {
        if (((ExpandedEntityData) this).playroom$isSlowedDown()) {
            return original * ServerConfig.instance().freezeSlowdown;
        }
        return original;
    }
}
