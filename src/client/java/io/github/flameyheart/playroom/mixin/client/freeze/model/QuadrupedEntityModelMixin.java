package io.github.flameyheart.playroom.mixin.client.freeze.model;

import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.duck.client.FreezableModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(QuadrupedEntityModel.class)
public abstract class QuadrupedEntityModelMixin implements FreezableModel {
    @Unique
    protected ModelPart playroom$root;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void storeRoot0(ModelPart root, boolean headScaled, float childHeadYOffset, float childHeadZOffset, float invertedChildHeadScale, float invertedChildBodyScale, int childBodyYOffset, CallbackInfo ci) {
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

    @Inject(method = "setAngles", at = @At("TAIL"))
    private void stopAnimations(@Coerce Object entity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (entity instanceof ExpandedEntityData eEntity) {
            playroom$stopAnimation(eEntity);
        }
    }
}
