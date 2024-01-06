package io.github.flameyheart.playroom.mixin.client.laserGun;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.config.ClientConfig;
import net.minecraft.client.Mouse;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mouse.class)
public class MouseMixin {
    @ModifyExpressionValue(
      method = "updateMouse",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;",
        ordinal = 0
      )
    )
    private Object applyRelativeSensitivity(Object genericValue) {
        double value = (Double) genericValue;
        return value / MathHelper.lerp(ClientConfig.instance().laserAimCameraSmoothness / 100.0, 1.0, PlayroomClient.getPreviousAimZoomDivisor());
    }
}
