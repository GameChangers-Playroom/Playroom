package io.github.flameyheart.playroom.mixin.client.freeze.model.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flameyheart.playroom.util.MixinUtil;
import net.minecraft.client.render.entity.SalmonEntityRenderer;
import net.minecraft.entity.passive.SalmonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SalmonEntityRenderer.class)
public abstract class SalmonMixin {
    @WrapOperation(method = "setupTransforms(Lnet/minecraft/entity/passive/SalmonEntity;Lnet/minecraft/client/util/math/MatrixStack;FFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;sin(F)F"))
    private float stopAnimations$Salmon(float value, Operation<Float> original, SalmonEntity entity) {
        return MixinUtil.playroom$cancelMath(value, original, entity);
    }
}
