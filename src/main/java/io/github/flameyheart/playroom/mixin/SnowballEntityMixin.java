package io.github.flameyheart.playroom.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SnowballEntity.class)
public class SnowballEntityMixin {
    @WrapOperation(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private boolean restoreKnockback(Entity instance, DamageSource source, float amount, Operation<Boolean> original) {
        if (amount == 0) {
            amount = Float.MIN_VALUE;
        }
        return original.call(instance, source, amount);
    }
}
