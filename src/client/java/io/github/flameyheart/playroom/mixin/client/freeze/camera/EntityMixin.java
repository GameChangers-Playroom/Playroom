package io.github.flameyheart.playroom.mixin.client.freeze.camera;

import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.freeze.CameraEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void updateCameraRotation(double yawChange, double pitchChange, CallbackInfo ci) {
        if (PlayroomClient.cameraEnabled) {
            CameraEntity camera = CameraEntity.getCamera();

            if (camera != null) {
                camera.updateCameraRotations((float) yawChange, (float) pitchChange);
            }

            ci.cancel();
        }
    }
}
