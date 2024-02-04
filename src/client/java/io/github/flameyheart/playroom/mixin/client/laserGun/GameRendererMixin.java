package io.github.flameyheart.playroom.mixin.client.laserGun;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.config.ClientConfig;
import io.github.flameyheart.playroom.item.LaserGun;
import io.github.flameyheart.playroom.item.OldLaserGun;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final MinecraftClient client;

    @ModifyArgs(method = "bobView", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"))
    private void changeSwingDistance(Args args) {

        // args.$0 is x-axis; args.$1 is y-axis

        if(client.options.getPerspective().isFirstPerson()) {
            if(PlayroomClient.isAiming(this.client.player.getMainHandStack())) {
                args.set(0, 0f);
                args.set(1, 0f);
            }
            else if(this.client.player.getMainHandStack().getItem() instanceof LaserGun || this.client.player.getMainHandStack().getItem() instanceof OldLaserGun) {
                args.set(0, ((float) args.get(0)) * .4f);
                args.set(1, ((float) args.get(1)) * .3f);
            }
        }


    }

    @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    private double modifyFovWithZoom(double fov, Camera camera, float tickDelta, boolean changingFov) {
        return fov / PlayroomClient.getAimZoomDivisor(tickDelta);
    }

    @ModifyExpressionValue(
      method = "renderHand",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/render/GameRenderer;getFov(Lnet/minecraft/client/render/Camera;FZ)D"
      )
    )
    private double keepHandFov(double fov, MatrixStack pose, Camera camera, float tickDelta) {
        if (!ClientConfig.instance().laserAimHandFov)
            return fov * PlayroomClient.getAimZoomDivisor(tickDelta);
        return fov;
    }
}
