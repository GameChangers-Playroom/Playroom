package io.github.flameyheart.playroom.mixin.client.freeze.model;

import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.duck.client.FreezableModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AbstractZombieModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractZombieModel.class)
public abstract class AbstractZombieModelMixin implements FreezableModel {
    @Unique
    protected ModelPart playroom$root;

    @Inject(method = "<init>(Lnet/minecraft/client/model/ModelPart;)V", at = @At("TAIL"))
    private void storeRoot(ModelPart root, CallbackInfo ci) {
        playroom$captureRoot(root);
    }

    @Unique
    private void playroom$captureRoot(ModelPart root) {
        if (root == null) throw new NullPointerException("Root is null for " + this.getClass().getSimpleName());
        this.playroom$root = root;
    }

    @Override
    public ModelPart playroom$getRoot() {
        return playroom$root;
    }

    @Inject(method = "setAngles(Lnet/minecraft/entity/mob/HostileEntity;FFFFF)V", at = @At("TAIL"))
    private void stopAnimations(@Coerce Object entity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (entity instanceof ExpandedEntityData eEntity) {
            playroom$stopAnimation(eEntity);
        }
    }
}
