package io.github.flameyheart.playroom.mixin.client.freeze.camera;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flameyheart.playroom.PlayroomClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LivingEntityRenderer.class, priority = 1002)
public class LivingEntityRendererMixin {

    //Allow rendering the client player entity by spoofing one of the entity rendering conditions while frozen
    @WrapOperation(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;)Z", require = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;"))
    private Entity fixLabel(MinecraftClient client, Operation<Entity> original) {
        if (PlayroomClient.orbitCameraEnabled && client.player != null) {
            return client.player;
        }

        return original.call(client);
    }
}
