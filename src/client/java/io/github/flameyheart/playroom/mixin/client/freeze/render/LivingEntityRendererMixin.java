package io.github.flameyheart.playroom.mixin.client.freeze.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.flameyheart.playroom.duck.FreezableEntity;
import io.github.flameyheart.playroom.render.entity.feature.IceFeatureRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Shadow protected abstract boolean addFeature(FeatureRenderer<T, M> feature);
    @Unique private LivingEntity playroom$entity;

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>", at = @At("TAIL"))
    private void addIceFeatures(EntityRendererFactory.Context ctx, EntityModel<T> model, float shadowRadius, CallbackInfo ci) {
        addFeature(new IceFeatureRenderer<>((LivingEntityRenderer<T, M>) (Object) this));
    }

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), argsOnly = true)
    private LivingEntity captureEntity(LivingEntity entity) {
        playroom$entity = entity;
        return entity;
    }

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void setColour(Args args) {
        if(playroom$entity instanceof FreezableEntity entity) {
            if (entity.playroom$isFrozen()) {
                //Fade from blue to white as the frozen ticks go down
                float colour = Math.max(1 - entity.playroom$getMeltProgress() * 2, 0.35f);
                args.set(4, colour);
                args.set(5, colour);
            }
        }
    }

    @ModifyExpressionValue(method = "setupTransforms", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;deathTime:I"))
    private int disableDeathTilt(int original) {
        if(playroom$entity instanceof FreezableEntity entity) {
            if (entity.playroom$isFrozen() && original > 0) {
                return 0;
            }
        }
        return original;
    }
}
