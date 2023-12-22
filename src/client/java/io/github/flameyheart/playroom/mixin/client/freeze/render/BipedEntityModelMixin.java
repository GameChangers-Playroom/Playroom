package io.github.flameyheart.playroom.mixin.client.freeze.render;

import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin<T extends LivingEntity> {
    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("HEAD"), cancellable = true)
    private void stopAnimations(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        ExpandedEntityData entity = (ExpandedEntityData) livingEntity;
        if (entity.playroom$isFrozen()) {
            if (PlayroomClient.wasFrozen) {
                ci.cancel();
            } else {
                PlayroomClient.wasFrozen = true;
            }
        }
    }
}
