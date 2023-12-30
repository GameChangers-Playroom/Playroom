package io.github.flameyheart.playroom.mixin.client.freeze.camera;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.config.ClientConfig;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GameRenderer.class, priority = 1002)
public class GameRendererMixin {
    @Shadow @Final MinecraftClient client;
    private @Unique double playroom$lastFreezeFov;
    private @Unique double playroom$freezeFov;

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

        ExpandedEntityData entity = (ExpandedEntityData) client.player;
        boolean zoom = entity.playroom$showZoom();
        //ZoomManager.ZOOM_HELPER.tick(zoom, 0);
        if (zoom) {
            double targetFov = ServerConfig.instance().freezeZoomFov;
            playroom$lastFreezeFov = playroom$freezeFov;
            this.playroom$freezeFov += (targetFov - this.playroom$freezeFov) * ((1 - targetFov) / (entity.playroom$zoomDuration() + 10));


            if (changingFov) {
                fov = this.client.options.getFov().getValue().intValue();
                fov *= MathHelper.lerp(tickDelta, playroom$lastFreezeFov, playroom$freezeFov);
            }

        } else {
            playroom$freezeFov = 1;
        }

        return fov;
    }
}
