package io.github.flameyheart.playroom.mixin.client.laserGun;

import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.item.Aimable;
import io.github.flameyheart.playroom.item.LaserGun;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    @Shadow public abstract void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);
    @Shadow protected abstract void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress);

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
    private void cancelFirstPersonRender(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        //Cancel item offhand render in first person
        if (hand == Hand.OFF_HAND && player.getMainHandStack().getItem() instanceof Aimable) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "HEAD"), cancellable = true)
    private void fixInteractionAnimation(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (item.getItem() instanceof LaserGun && !player.isUsingItem()) {
            boolean mainHand = hand == Hand.MAIN_HAND;
            if (!mainHand) return;
            Arm arm = player.getMainArm();
            matrices.push();
            boolean rightHanded = arm == Arm.RIGHT;
            this.applyEquipOffset(matrices, arm, 0);
            float f = MathHelper.sqrt(swingProgress);
            float g = -0.2f * MathHelper.sin(swingProgress * (float)Math.PI);
            float h = -0.4f * MathHelper.sin(f * (float)Math.PI);
            matrices.translate(0, 0, g * h / -2);
            this.renderItem(player, item, rightHanded ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !rightHanded, matrices, vertexConsumers, light);
            matrices.pop();
            ci.cancel();
        }
    }
}
