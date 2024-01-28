package io.github.flameyheart.playroom.mixin.client.freeze.camera;

import com.mojang.authlib.GameProfile;
import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.freeze.CameraEntity;
import io.github.flameyheart.playroom.freeze.DummyMovementInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Shadow public Input input;
    private @Unique Input playroom$realInput;
    private final @Unique DummyMovementInput playroom$dummyMovementInput = new DummyMovementInput(null);

    @Inject(method = "swingHand", at = @At("HEAD"), cancellable = true)
    private void preventHandSwing(Hand hand, CallbackInfo ci) {
        if (PlayroomClient.orbitCameraEnabled) {
            ci.cancel();
        }
    }

    /** This does nothing? */
    @Inject(method = "isCamera", at = @At("HEAD"), cancellable = true)
    private void allowPlayerMovementInFreeCameraMode(CallbackInfoReturnable<Boolean> cir) {
        if (PlayroomClient.orbitCameraEnabled && CameraEntity.originalCameraWasPlayer()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void disableMovementInputsPre(CallbackInfo ci) {
        if (PlayroomClient.orbitCameraEnabled) {
            this.playroom$realInput = this.input;
            this.input = this.playroom$dummyMovementInput;
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void disableMovementInputsPost(CallbackInfo ci) {
        if (this.playroom$realInput != null) {
            this.input = this.playroom$realInput;
            this.playroom$realInput = null;
        }
    }
}
