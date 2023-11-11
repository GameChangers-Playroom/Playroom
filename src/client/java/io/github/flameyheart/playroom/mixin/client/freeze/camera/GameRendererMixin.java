package io.github.flameyheart.playroom.mixin.client.freeze.camera;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flameyheart.playroom.PlayroomClient;
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
public abstract class GameRendererMixin {

    @Shadow @Final MinecraftClient client;

    @Shadow public abstract void render(float tickDelta, long startTime, boolean tick);

    private @Unique float playroom$lastScale;

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
        if (client.player == null) return fov;

        ExpandedEntityData entity = (ExpandedEntityData) client.player;
        if (entity.playroom$getGunFreezeTicks() > 0) {
            //The higher the frozen ticks the closer to 1 scale is, the lower the frozen ticks the closer to 0 scale is
            float scale = MathHelper.clamp(entity.playroom$getGunFreezeTicks() / 80f, 0, 1);
            float prevScale = scale >= 1 ? 1 : playroom$lastScale;
            playroom$lastScale = scale;

            //Make it smoother by using tick delta to interpolate between the current fov and the fov scale
            return fov * MathHelper.lerp(MinecraftClient.getInstance().getLastFrameDuration(), prevScale, scale);
        } else if (PlayroomClient.orbitCameraEnabled && !PlayroomClient.forceOrbitCamera) {
            return 0.001;
        }
        return fov;
    }
}
