package io.github.flameyheart.playroom.mixin.client;

import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Shadow @Final protected EntityRenderDispatcher dispatcher;

    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "renderLabelIfPresent", at = @At("TAIL"))
    private void renderFancyLabel(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (entity instanceof PlayerEntity player) {
            Text prefix = ((ExpandedEntityData) player).playroom$getDisplayName().getLeft();
            boolean bl = !entity.isSneaky();
            if (!bl || prefix == null || text == null) {
                return;
            }
            float f = entity.getNameLabelHeight();
            int i = "deadmau5".equals(text.getString()) ? -10 : 0;

            matrices.push();
            matrices.translate(0.0f, f + 0.25f, 0.0f);
            matrices.multiply(this.dispatcher.getRotation());
            matrices.scale(-0.025f, -0.025f, 0.025f);
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25f);
            int j = (int)(g * 255.0f) << 24;
            TextRenderer textRenderer = this.getTextRenderer();
            float h = (float) -textRenderer.getWidth(prefix) / 2;
            textRenderer.draw(prefix, h, i, 0x20FFFFFF,false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, j, light);
            textRenderer.draw(prefix, h, i, -1, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
            matrices.pop();
        }
    }
}
