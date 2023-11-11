package io.github.flameyheart.playroom.mixin;

import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
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
        if (((ExpandedEntityData) this).playroom$isFrozen()) {
            ((ExpandedEntityData) this).playroom$addGunFreezeTicks(-(int) Math.ceil(fallDistance * 3f)); //TODO: config
            return damageMultiplier * 0.3f; //TODO: config
        }
        return damageMultiplier;
    }

}
