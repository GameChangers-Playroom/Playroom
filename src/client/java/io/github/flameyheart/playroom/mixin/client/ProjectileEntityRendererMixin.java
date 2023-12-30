package io.github.flameyheart.playroom.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flameyheart.playroom.render.entity.LaserProjectileRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProjectileEntityRenderer.class)
public class ProjectileEntityRendererMixin {

    @WrapOperation(method = "render(Lnet/minecraft/entity/projectile/PersistentProjectileEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getEntityCutout(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"))
    private RenderLayer removeDrag(Identifier texture, Operation<RenderLayer> original) {
        if (((ProjectileEntityRenderer) (Object) this) instanceof LaserProjectileRenderer) {
            return RenderLayer.getEyes(texture);
        } else {
            return original.call(texture);
        }
    }
}
