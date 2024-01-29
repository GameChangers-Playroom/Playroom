package io.github.flameyheart.playroom.mixin.client.laserGun;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.config.ClientConfig;
import io.github.flameyheart.playroom.item.LaserGun;
import io.github.flameyheart.playroom.item.OldLaserGun;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final MinecraftClient client;

    @Inject(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;bobView(Lnet/minecraft/client/util/math/MatrixStack;F)V", shift = At.Shift.BEFORE))
    private void bobViewGun(MatrixStack matrices, Camera camera, float tickDelta, CallbackInfo ci) {
        if (!PlayroomClient.isAiming(this.client.player.getMainHandStack())) {
            playroom$gunBobView(matrices, tickDelta);
        }
    }

    @SuppressWarnings("unused")
    @WrapWithCondition(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;bobView(Lnet/minecraft/client/util/math/MatrixStack;F)V"))
    private boolean vanillaBobView(GameRenderer instance, MatrixStack matrices, float tickDelta) {
        return !(this.client.player.getMainHandStack().getItem() instanceof LaserGun || this.client.player.getMainHandStack().getItem() instanceof OldLaserGun);
    }

    private @Unique void playroom$gunBobView(MatrixStack matrices, float tickDelta) {
        if (!(this.client.getCameraEntity() instanceof PlayerEntity playerEntity)) {
            return;
        }
        float f = (playerEntity.horizontalSpeed - playerEntity.prevHorizontalSpeed);
        float g = -(playerEntity.horizontalSpeed + f * tickDelta);
        float h = MathHelper.lerp(tickDelta, playerEntity.prevStrideDistance, playerEntity.strideDistance) * 0.25f;

        matrices.translate(MathHelper.sin(g * (float) Math.PI) * h * 0.5f, -Math.abs(MathHelper.cos(g * (float) Math.PI) * h), 0.0);

        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(g * (float) Math.PI) * h * 3.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(Math.abs(MathHelper.cos(g * (float) Math.PI - 0.2f) * h) * 5.0f));
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
