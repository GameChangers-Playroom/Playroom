package io.github.flameyheart.playroom.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.github.flameyheart.playroom.entity.LaserProjectileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PersistentProjectileEntity.class)
public class PersistentProjectileEntityMixin {

    @SuppressWarnings("ConstantValue")
    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    private boolean removeDrag(PersistentProjectileEntity instance, Vec3d vec3d) {
        return !(((PersistentProjectileEntity) (Object) this) instanceof LaserProjectileEntity);
    }
}
