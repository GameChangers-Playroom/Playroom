package io.github.flameyheart.playroom.mixin.client.freeze.camera;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.config.ClientConfig;
import io.github.flameyheart.playroom.duck.FreezableEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GameRenderer.class, priority = 1002)
public class GameRendererMixin {
    @Shadow @Final MinecraftClient client;

    //Uses player target so the camera doesn't target the player and allows you to kick yourself with a single click
    @WrapOperation(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;"))
    private Entity usePlayerTarget(MinecraftClient client, Operation<Entity> original) {
        if (PlayroomClient.orbitCameraEnabled && client.player != null) {
            return client.player;
        }

        return original.call(client);
    }

    @ModifyReturnValue(method = "getFov", at = @At(value = "RETURN", ordinal = 1))
    private double applyZoom(double fov, Camera camera, float tickDelta, boolean changingFov) {
        if (client.player == null || ClientConfig.instance().reducedMotion) return fov;

        FreezableEntity entity = (FreezableEntity) client.player;
        boolean zoom = entity.playroom$getFreezeTime() - 1 - tickDelta <= ClientConfig.instance().freezeZoomDuration * 20;
        if (zoom) {
            if (!PlayroomClient.hasUnfreezeZoom()) PlayroomClient.setUnfreezeZoom(true);
            return fov / PlayroomClient.UNFREEZE_ZOOM.getZoomDivisor(tickDelta);
        } else {
            if (PlayroomClient.hasUnfreezeZoom()) {
                PlayroomClient.setUnfreezeZoom(false);
                PlayroomClient.UNFREEZE_ZOOM.setToZero();
            }
        }

        return fov;
    }
}
