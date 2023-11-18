package io.github.flameyheart.playroom.render.entity;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.entity.LaserProjectileEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.util.Identifier;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Environment(value = EnvType.CLIENT)
public class LaserProjectileRenderer extends ProjectileEntityRenderer<LaserProjectileEntity> {
    public static final Identifier RANGED_TEXTURE = Playroom.id("textures/entity/projectiles/range.png");
    public static final Identifier RAPID_FIRE_TEXTURE = Playroom.id("textures/entity/projectiles/rapid-fire.png");
    public static final float TEXTURE_WIDTH = 24;
    public static final float TEXTURE_HEIGHT = 7;

    public LaserProjectileRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(LaserProjectileEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public Identifier getTexture(LaserProjectileEntity entity) {
        return entity.isRapidFire() ? RAPID_FIRE_TEXTURE : RANGED_TEXTURE;
    }

    @Override
    public void vertex(Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertexConsumer, int x, int y, int z, float u, float v, int normalX, int normalZ, int normalY, int light) {
        if (Math.abs(u - 0.5) < 1e-8) u = TEXTURE_WIDTH / 32;
        if (Math.abs(v - 0.15625F) < 1e-8) v = TEXTURE_HEIGHT / 32;
        vertexConsumer.vertex(positionMatrix, x, y, z)
          .color(255, 255, 255, 100)
          .texture(u, v)
          .overlay(OverlayTexture.DEFAULT_UV)
          .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
          .normal(normalMatrix, normalX, normalY, normalZ)
          .next();
    }
}