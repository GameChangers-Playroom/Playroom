package io.github.flameyheart.playroom.render.entity.feature;

import io.github.flameyheart.playroom.duck.LivingEntityExtender;
import io.github.flameyheart.playroom.entity.IceSpearEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.StuckObjectsFeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class IceFeatureRenderer<T extends LivingEntity, M extends PlayerEntityModel<T>> extends StuckObjectsFeatureRenderer<T, M> {
    private final EntityRenderDispatcher dispatcher;

    public IceFeatureRenderer(EntityRendererFactory.Context context, LivingEntityRenderer<T, M> entityRenderer) {
        super(entityRenderer);
        this.dispatcher = context.getRenderDispatcher();
    }

    @Override
    protected int getObjectCount(T entity) {
        return ((LivingEntityExtender) entity).playroom$getStuckSpearCount();
    }

    @Override
    protected void renderObject(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Entity entity, float directionX, float directionY, float directionZ, float tickDelta) {
        float normalisedDir = MathHelper.sqrt(directionX * directionX + directionZ * directionZ);
        IceSpearEntity iceSpearEntity = new IceSpearEntity(entity.getWorld(), entity.getX(), entity.getY(), entity.getZ());
        iceSpearEntity.setYaw((float) (Math.atan2(directionX, directionZ) * 57.295776F));
        iceSpearEntity.setPitch((float) (Math.atan2(directionY, normalisedDir) * 57.295776F));
        iceSpearEntity.prevYaw = iceSpearEntity.getYaw();
        iceSpearEntity.prevPitch = iceSpearEntity.getPitch();
        dispatcher.render(iceSpearEntity, 0, 0, 0, 0, tickDelta, matrices, vertexConsumers, light);
    }
}
