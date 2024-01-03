package io.github.flameyheart.playroom.mixin.client.freeze.model;

import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.duck.client.FreezableModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin implements FreezableModel {
    @Unique
    protected ModelPart playroom$root;

    @Inject(method = "<init>(Lnet/minecraft/client/model/ModelPart;Z)V", at = @At("TAIL"))
    private void storeRoot(ModelPart root, boolean thinArms, CallbackInfo ci) {
        captureRoot(root);
    }

    @Unique
    private void captureRoot(ModelPart root) {
        if (root == null) throw new NullPointerException("Root is null for " + this.getClass().getSimpleName());
        this.playroom$root = root;
    }

    @Override
    public ModelPart playroom$getRoot() {
        return playroom$root;
    }

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void stopAnimations(@Coerce Object entity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (entity instanceof ExpandedEntityData eEntity) {
            playroom$stopAnimation(eEntity);
        }
    }
}
