package io.github.flameyheart.playroom.mixin.client.freeze.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.flameyheart.playroom.duck.FreezableEntity;
import io.github.flameyheart.playroom.duck.FreezeOverlay;
import io.github.flameyheart.playroom.render.entity.feature.old.OldIceFeatureRenderer;
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

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>", at = @At("TAIL"))
    private void addIceFeatures(EntityRendererFactory.Context ctx, EntityModel<T> model, float shadowRadius, CallbackInfo ci) {
        addFeature(new OldIceFeatureRenderer<>((LivingEntityRenderer<T, M>) (Object) this));
    }

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void setColour(Args args, @Local(argsOnly = true) LivingEntity entity) {
        if (entity instanceof FreezeOverlay freezable) {
            if (freezable.playroom$showOverlay()) {
                //Fade from blue to white as the frozen ticks go down
                float colour = Math.max(1 - freezable.playroom$getOverlayProgress() * 2, 0.35f);
                args.set(4, colour);
                args.set(5, colour);
            }
        }
    }

    @ModifyExpressionValue(method = "setupTransforms", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;deathTime:I"))
    private int disableDeathTilt(int original, @Local(argsOnly = true) LivingEntity entity) {
        if(entity instanceof FreezableEntity freezable) {
            if (freezable.playroom$isFrozen() && original > 0) {
                return 0;
            }
        }
        return original;
    }
}
