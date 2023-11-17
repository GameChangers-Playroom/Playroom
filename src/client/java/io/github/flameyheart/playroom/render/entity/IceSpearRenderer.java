package io.github.flameyheart.playroom.render.entity;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.entity.IceSpearEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class IceSpearRenderer extends ProjectileEntityRenderer<IceSpearEntity> {
    public static final Identifier TEXTURE = Playroom.id("textures/entity/projectiles/range.png");

    public IceSpearRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(IceSpearEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public Identifier getTexture(IceSpearEntity arrowEntity) {
        return TEXTURE;
    }
}