package io.github.flameyheart.playroom.render.entity.feature;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.registry.Items;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public class IceFeatureRenderer<T extends AbstractClientPlayerEntity> extends FeatureRenderer<T, PlayerEntityModel<T>> {
    private final EntityRendererFactory.Context ctx;

    public IceFeatureRenderer(EntityRendererFactory.Context ctx, LivingEntityRenderer<T, PlayerEntityModel<T>> context) {
        super(context);
        this.ctx = ctx;
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!((ExpandedEntityData) entity).playroom$showIce()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ExpandedEntityData eEntity = (ExpandedEntityData) entity;
        float iceMelt = eEntity.playroom$iceMeltProgress();

        PlayerEntityModel<T> playerModel = getContextModel();
        matrixStack.push();
        playerModel.body.rotate(matrixStack);
        matrixStack.translate(0, 0.9, 0);
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotation((float) Math.PI));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float) Math.PI));
        ModelIdentifier modelId = new ModelIdentifier(Playroom.MOD_ID, "ice_blocks", "inventory");
        ItemRenderer itemRenderer = client.getItemRenderer();
        matrixStack.translate(0,  -(1 - iceMelt) / 2, 0);
        matrixStack.scale(1, iceMelt, 1);
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
