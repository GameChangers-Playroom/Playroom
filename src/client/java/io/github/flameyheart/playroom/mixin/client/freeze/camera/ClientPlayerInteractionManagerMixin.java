package io.github.flameyheart.playroom.mixin.client.freeze.camera;

import io.github.flameyheart.playroom.PlayroomClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(method = "interactBlock", at = @At(value = "HEAD"), cancellable = true)
    private void disableBlockInteraction(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (PlayroomClient.orbitCameraEnabled) {
            cir.setReturnValue(ActionResult.PASS);
            cir.cancel();
        }
    }

    @Inject(method = "interactItem", at = @At(value = "HEAD"), cancellable = true)
    private void disableItemInteraction(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (PlayroomClient.orbitCameraEnabled) {
            cir.setReturnValue(ActionResult.PASS);
            cir.cancel();
        }
    }

    @Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
    private void disableEntityInteraction1(PlayerEntity player, Entity target, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (PlayroomClient.orbitCameraEnabled) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "interactEntityAtLocation", at = @At("HEAD"), cancellable = true)
    private void disableEntityInteraction2(PlayerEntity player, Entity target, EntityHitResult trace, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (PlayroomClient.orbitCameraEnabled) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void disableEntityInteraction3(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (PlayroomClient.orbitCameraEnabled) {
            ci.cancel();
        }
    }

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void disableBlockBreaking1(BlockPos pos, Direction side, CallbackInfoReturnable<Boolean> cir) {
        if (PlayroomClient.orbitCameraEnabled) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"), cancellable = true)
    private void disableBlockBreaking2(BlockPos pos, Direction side, CallbackInfoReturnable<Boolean> cir) {
        if (PlayroomClient.orbitCameraEnabled) {
            cir.setReturnValue(false);
        }
    }
}
