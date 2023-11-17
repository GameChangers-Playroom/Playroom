package io.github.flameyheart.playroom.render.entity;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.entity.IceSpearEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Environment(value=EnvType.CLIENT)
public class IceSpearRenderer extends EntityRenderer<IceSpearEntity> {
    public static final Identifier TEXTURE = Playroom.id("textures/entity/projectiles/range.png");
    public static float x = 0;
    public static float y = 0;
    public static float z = 0;

    public IceSpearRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(IceSpearEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(IceSpearEntity entity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw()) - 90));
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch())));
        //matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angle));

        //float s = (float)((IceSpearEntity)entity).shake - g;
        /*if (s > 0.0f) {
            float t = -MathHelper.sin(s * 3.0f) * s;
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(t));
        }*/

        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(x));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(y));
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(z));
        matrixStack.scale(0.05625f, 0.05625f, 0.05625f);
        //matrixStack.translate(-4.0f, 0.0f, 0.0f);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutout(this.getTexture(entity)));
        MatrixStack.Entry entry = matrixStack.peek();
        Matrix4f matrix4f = entry.getPositionMatrix();
        Matrix3f matrix3f = entry.getNormalMatrix();


        for (int j = 0; j < 4; j++) {
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0f));
            this.vertex(matrix4f, matrix3f, vertexConsumer, 0, -23, -6, 1, 0);
            this.vertex(matrix4f, matrix3f, vertexConsumer, 0, -23,  6, 1, 1);
            this.vertex(matrix4f, matrix3f, vertexConsumer, 0,  23,  6, 0, 1);
            this.vertex(matrix4f, matrix3f, vertexConsumer, 0,  23, -6, 0, 0);
        }

        matrixStack.pop();
        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, i);
    }

    public void vertex(Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertexConsumer, int x, int y, int z, float u, float v) {
        vertexConsumer.vertex(positionMatrix, x, y, z).color(255, 255, 255, 255).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(normalMatrix, 0, 1, 0).next();
    }

    @Override
    public Identifier getTexture(IceSpearEntity arrowEntity) {
        return TEXTURE;
    }
}