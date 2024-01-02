package io.github.flameyheart.playroom.mixin.client.freeze.model.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flameyheart.playroom.util.MixinUtil;
import net.minecraft.client.render.entity.CodEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CodEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CodEntityRenderer.class)
public abstract class CodMixin {
    @WrapOperation(method = "setupTransforms(Lnet/minecraft/entity/passive/CodEntity;Lnet/minecraft/client/util/math/MatrixStack;FFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;sin(F)F"))
    private float stopAnimations$Cod(float value, Operation<Float> original, CodEntity entity) {
        return MixinUtil.playroom$cancelMath(value, original, entity);
    }
}
