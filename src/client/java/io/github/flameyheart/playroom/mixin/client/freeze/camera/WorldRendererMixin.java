package io.github.flameyheart.playroom.mixin.client.freeze.camera;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flameyheart.playroom.PlayroomClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    //Allow rendering the client player entity by spoofing one of the entity rendering conditions
    @WrapOperation(method = "render", require = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getFocusedEntity()Lnet/minecraft/entity/Entity;", ordinal = 3))
    private Entity allowRenderingClientPlayer(Camera camera, Operation<Entity> original) {
        if (PlayroomClient.cameraEnabled) {
            return MinecraftClient.getInstance().player;
        }

        return original.call(camera);
    }
}
