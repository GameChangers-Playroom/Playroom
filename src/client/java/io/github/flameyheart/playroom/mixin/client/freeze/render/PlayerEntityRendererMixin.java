package io.github.flameyheart.playroom.mixin.client.freeze.render;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.render.entity.feature.IceFeatureRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addIceFeatures(EntityRendererFactory.Context ctx, boolean slim, CallbackInfo ci) {
        this.addFeature(new IceFeatureRenderer(ctx, this));
    }

    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private void changeRenderColor1(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        ExpandedEntityData entity = (ExpandedEntityData) abstractClientPlayerEntity;
        int freezeTicks = entity.playroom$getGunFreezeTicks();
        if (entity.playroom$isFrozen()) {
            //Fade from blue to white as the frozen ticks go down, blue is always 1, use `RenderSystem.setShaderColor(red, green, blue, alpha);`
            float blue = 1;
            float color = Math.max(1 - freezeTicks / 20f, 0.3f);
            float alpha = 1;
            RenderSystem.setShaderColor(color, color, blue, alpha);
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    private void changeRenderColor2(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        ExpandedEntityData entity = (ExpandedEntityData) abstractClientPlayerEntity;
        if (entity.playroom$isFrozen()) {
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }
}
