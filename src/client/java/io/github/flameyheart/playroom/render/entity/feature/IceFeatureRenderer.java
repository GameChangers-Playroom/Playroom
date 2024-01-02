package io.github.flameyheart.playroom.render.entity.feature;

import io.github.flameyheart.playroom.ClientConstants;
import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
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

public class IceFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
    public IceFeatureRenderer(LivingEntityRenderer<T, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!((ExpandedEntityData) entity).playroom$showIce()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        matrixStack.push();

        ExpandedEntityData eEntity = (ExpandedEntityData) entity;
        float iceMelt = eEntity.playroom$iceMeltProgress();
        float entityHeight = entity.getHeight();
        float entityWidth = entity.getWidth();
        float scaleFactor = (entityHeight / 1.8f + entityWidth / 0.6f) / 2;  // model designed for 1.8x0.6 entity

        matrixStack.multiply(RotationAxis.POSITIVE_X.rotation((float) Math.PI));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float) Math.PI));
        if (entity.getType().equals(EntityType.ALLAY)) {
            matrixStack.translate(0, -1.4, 0);
        } else {
            matrixStack.translate(0, -1, 0);
        }

        ModelIdentifier modelId = new ModelIdentifier(Playroom.MOD_ID, "ice_blocks", "inventory");
        ItemRenderer itemRenderer = client.getItemRenderer();
        //TODO: DON'T LEAVE THIS COMMENTED
        //matrixStack.translate(0,  scaleFactor * -(1 - iceMelt) / 2, 0);
        if (!ClientConstants.IGNORE_ICE_SCALE.contains(entity.getType())) {
            matrixStack.scale(scaleFactor, scaleFactor * iceMelt, scaleFactor);
        }
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
}
