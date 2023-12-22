package io.github.flameyheart.playroom.mixin.client.laserGun;

import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.item.Aimable;
import io.github.flameyheart.playroom.item.LaserGun;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
    private void cancelFirstPersonRender(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        //Cancel item offhand render in first person
        if (hand == Hand.OFF_HAND && player.getMainHandStack().getItem() instanceof Aimable) {
            ci.cancel();
        }

        //IF PLAYER AIMS WITH SCOPED GUN
        if (PlayroomClient.isAiming(player.getMainHandStack())) {
            ci.cancel();
        }
    }
}