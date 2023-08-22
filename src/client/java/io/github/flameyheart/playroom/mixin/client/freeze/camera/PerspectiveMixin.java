package io.github.flameyheart.playroom.mixin.client.freeze.camera;

import io.github.flameyheart.playroom.PlayroomClient;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Perspective.class)
public class PerspectiveMixin {

    @Inject(method = "isFirstPerson", at = @At("HEAD"), cancellable = true)
    private void force3rdPerson(CallbackInfoReturnable<Boolean> cir) {
        if (PlayroomClient.cameraEnabled) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isFrontView", at = @At("HEAD"), cancellable = true)
    private void forceBackView(CallbackInfoReturnable<Boolean> cir) {
        if (PlayroomClient.cameraEnabled) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "next", at = @At("HEAD"), cancellable = true)
    private void disableCycling(CallbackInfoReturnable<Perspective> cir) {
        if (PlayroomClient.cameraEnabled) {
            cir.setReturnValue((Perspective) (Object) this);
        }
    }
}
