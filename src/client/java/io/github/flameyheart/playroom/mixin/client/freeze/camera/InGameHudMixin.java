package io.github.flameyheart.playroom.mixin.client.freeze.camera;

import io.github.flameyheart.playroom.PlayroomClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "getCameraPlayer", at = @At("HEAD"), cancellable = true)
    private void overridePlayerForRendering(CallbackInfoReturnable<PlayerEntity> cir) {
        // Fix the hotbar rendering in the orbit mode by using the actual player
        if (PlayroomClient.orbitCameraEnabled && this.client.player != null) {
            cir.setReturnValue(this.client.player);
        }
    }
}
