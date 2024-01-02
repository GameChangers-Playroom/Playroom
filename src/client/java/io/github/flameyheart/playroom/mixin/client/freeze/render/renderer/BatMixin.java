package io.github.flameyheart.playroom.mixin.client.freeze.render.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flameyheart.playroom.util.MixinUtil;
import net.minecraft.client.render.entity.BatEntityRenderer;
import net.minecraft.entity.passive.BatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BatEntityRenderer.class)
public class BatMixin {
    @WrapOperation(method = "setupTransforms(Lnet/minecraft/entity/passive/BatEntity;Lnet/minecraft/client/util/math/MatrixStack;FFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;cos(F)F"))
    private float stopAnimations$Bat(float value, Operation<Float> original, BatEntity entity) {
        return MixinUtil.playroom$cancelMath(value, original, entity);
    }
}
