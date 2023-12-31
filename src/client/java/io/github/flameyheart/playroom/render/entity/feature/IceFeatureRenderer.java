package io.github.flameyheart.playroom.render.entity.feature;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.mixin.client.QuadrupedEntityModelAccessor;
import io.github.flameyheart.playroom.registry.Items;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RotationAxis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IceFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
    private final Map<Class<?>, ModelPart> bodyPartCache = new HashMap<>();
    private final Set<Class<?>> blacklist = new HashSet<>();

    public IceFeatureRenderer(LivingEntityRenderer<T, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!((ExpandedEntityData) entity).playroom$showIce()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        EntityModel<T> model = getContextModel();

        matrixStack.push();
//        if(model instanceof BipedEntityModel<T> bipedEntityModel) {
//            bipedEntityModel.body.rotate(matrixStack);
//        } else if (model instanceof QuadrupedEntityModelAccessor quadrupedEntityModel) {
//            quadrupedEntityModel.getBody().rotate(matrixStack);
//            matrixStack.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(90));
//        } else {
//            Class<?> modelClass = model.getClass();
//            if(!blacklist.contains(modelClass)) {
//                ModelPart bodyPart = bodyPartCache.get(modelClass);
//                if(bodyPart == null) {
//                    try {
//                        bodyPart = (ModelPart) modelClass.getField("body").get(model);
//                        bodyPartCache.put(modelClass, bodyPart);
//                    } catch (ReflectiveOperationException e) {
//                        blacklist.add(modelClass);
//                    }
//                }
//                if(bodyPart != null) {
//                    bodyPart.rotate(matrixStack);
//                }
//            }
//        }

        ExpandedEntityData eEntity = (ExpandedEntityData) entity;
        float iceMelt = eEntity.playroom$iceMeltProgress();
        float entityHeight = entity.getHeight();
        float entityWidth = entity.getWidth();
        float scaleFactor = (entityHeight / 1.8f + entityWidth / 0.6f) / 2;  // model designed for 1.8x0.6 entity

        matrixStack.multiply(RotationAxis.POSITIVE_X.rotation((float) Math.PI));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float) Math.PI));
        matrixStack.translate(0, -0.9f / scaleFactor, 0);

        ModelIdentifier modelId = new ModelIdentifier(Playroom.MOD_ID, "ice_blocks", "inventory");
        ItemRenderer itemRenderer = client.getItemRenderer();
        matrixStack.translate(0,  scaleFactor * -(1 - iceMelt) / 2, 0);
        matrixStack.scale(scaleFactor, scaleFactor * iceMelt, scaleFactor);
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
