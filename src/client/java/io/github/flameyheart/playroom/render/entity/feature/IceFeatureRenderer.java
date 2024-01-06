package io.github.flameyheart.playroom.render.entity.feature;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.duck.FreezableEntity;
import io.github.flameyheart.playroom.registry.Items;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RotationAxis;

import static net.minecraft.entity.EntityType.*;

public class IceFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
    public IceFeatureRenderer(LivingEntityRenderer<T, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!((FreezableEntity) entity).playroom$isFrozen()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        matrixStack.push();

        FreezableEntity eEntity = (FreezableEntity) entity;
        float iceMelt = eEntity.playroom$getMeltProgress();
        float entityHeight = entity.getHeight();
        float entityWidth = entity.getWidth();
        float scaleFactor = (entityHeight / 1.8f + entityWidth / 0.6f) / 2;  // model designed for 1.8x0.6 entity
        EntityType<?> type = entity.getType();

        matrixStack.multiply(RotationAxis.POSITIVE_X.rotation((float) Math.PI));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float) Math.PI));
        if (isOf(type, ALLAY, ENDERMAN, ENDERMITE, FROG, MAGMA_CUBE, PUFFERFISH, COD, RABBIT, SILVERFISH, SLIME, TADPOLE, TROPICAL_FISH, VEX, GHAST)) {
            matrixStack.translate(0, -1.4, 0);
        } else if (isOf(type, PHANTOM)) {
            matrixStack.translate(0, -0, 0);
        } else if (isOf(type, BAT)) {
            matrixStack.translate(0, -0.4, 0.5);
        } else if (isOf(type, SALMON)) {
            matrixStack.translate(0, -1.2, 0.5);
        } else {
            matrixStack.translate(0, -1, 0);
        }

        ModelIdentifier modelId = new ModelIdentifier(Playroom.MOD_ID, "ice_blocks", "inventory");
        ItemRenderer itemRenderer = client.getItemRenderer();
        //TODO: DON'T LEAVE THIS COMMENTED
        matrixStack.translate(0,  scaleFactor * -(1 - iceMelt) / 2, 0);
        if (isOf(type, ELDER_GUARDIAN, ENDER_DRAGON, GHAST, GIANT, WITHER)) {
            scaleFactor = 1.0f;
        } if (isOf(type, BAT)) {
            scaleFactor += 0.2f;
        } if (isOf(type, MAGMA_CUBE, SLIME)) {
            scaleFactor = 0.5f;
        }
        matrixStack.scale(scaleFactor, scaleFactor * iceMelt, scaleFactor);
        //matrixStack.translate(0, -entityHeight, 0);
        itemRenderer.renderItem(
                Items.ICE_BLOCKS.getDefaultStack(),
                ModelTransformationMode.NONE,
                false,
                matrixStack,
                vertexConsumers,
                light,
                OverlayTexture.DEFAULT_UV,
                itemRenderer.getModels().getModelManager().getModel(modelId)
        );
        matrixStack.pop();
    }

    private boolean isOf(EntityType<?> type, EntityType<?>... types) {
        for (EntityType<?> t : types) {
            if (type.equals(t)) return true;
        }
        return false;

    }
}
