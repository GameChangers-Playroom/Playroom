package io.github.flameyheart.playroom.render.entity.feature;

import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.random.Random;

public class IceFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    private final EntityRendererFactory.Context ctx;
    private Random random;

    //
    public IceFeatureRenderer(EntityRendererFactory.Context ctx, LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
        this.ctx = ctx;
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!((ExpandedEntityData) entity).playroom$isFrozen()) return;
        ExpandedEntityData eEntity = (ExpandedEntityData) entity;
        int freezeTicks = eEntity.playroom$getGunFreezeTicks();
        //MatrixStack stack = new MatrixStack();

        matrixStack.push();
        //float[] shaderColor = RenderSystem.getShaderColor();
        //int overlay = LivingEntityRenderer.getOverlay(entity, 0.0f);

        /*RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();*/
        //this.ctx.getBlockRenderManager().renderBlockAsEntity(Blocks.ICE.getDefaultState(), matrixStack, vertexConsumers, light, overlay);
        //this.ctx.getHeldItemRenderer().renderItem(entity, entity.getOffHandStack(), ModelTransformationMode.THIRD_PERSON_LEFT_HAND, false, matrixStack, vertexConsumers, light);
        /*Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

        stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180));

        stack.translate(-0.5, 0, -0.5);

        //Interpolate entity prev position with current position
        double x = MathHelper.lerp(tickDelta, entity.prevX, entity.getX());
        double y = MathHelper.lerp(tickDelta, entity.prevY, entity.getY());
        double z = MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ());

        Vec3d pos = new Vec3d(x, y, z);*/

        //Renderer3d.renderEdged(stack, new Color(0, 242, 250, 128), Color.RED, pos, new Vec3d(1, 1, 1));
        //RenderSystem.setShaderColor(0f, 1f, 0f, 1f);
        //ctx.getBlockRenderManager().renderBlock(Blocks.ICE.getDefaultState(), entity.getBlockPos(), entity.getWorld(), matrixStack, vertexConsumers.getBuffer(RenderLayer.getTranslucent()), false, entity.getRandom());

        //RenderSystem.disableDepthTest();
        //RenderSystem.disableCull();
        //RenderSystem.disableBlend();

        matrixStack.pop();
        //RenderSystem.setShaderColor(1f, 0f, 0f, 1f);
    }
}
