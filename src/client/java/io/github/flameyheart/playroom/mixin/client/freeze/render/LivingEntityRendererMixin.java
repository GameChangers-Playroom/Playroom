package io.github.flameyheart.playroom.mixin.client.freeze.render;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Unique
    private LivingEntity playroom$entity;

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), argsOnly = true)
    private LivingEntity captureEntity(LivingEntity entity) {
        playroom$entity = entity;
        return entity;
    }

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void setColour(Args args) {
        if(playroom$entity instanceof ExpandedEntityData entity) {
            int freezeTicks = entity.playroom$getGunFreezeTicks();
            if (entity.playroom$isFrozen()) {
                //Fade from blue to white as the frozen ticks go down, blue is always 1, use `RenderSystem.setShaderColor(red, green, blue, alpha);`
                float colour = Math.max(1 - freezeTicks / 20f, 0.3f);
                args.set(4, colour);
                args.set(5, colour);
            }
        }
    }
}
