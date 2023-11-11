package io.github.flameyheart.playroom.render.particle;

import me.x150.renderer.render.Renderer3d;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class TestParticle extends Particle {

    protected TestParticle(ClientWorld world, double x, double y, double z) {
        super(world, x, y, z);
        this.maxAge = 16;
        this.ascending = false;
        this.velocityY += 0.1;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Vec3d vec3d = new Vec3d(prevPosX, prevPosY, prevPosZ).lerp(new Vec3d(x, y, z), tickDelta);
        io.wispforest.owo.ui.core.Color owoColor = io.wispforest.owo.ui.core.Color.ofArgb(0x80FF00FF);
        owoColor = new io.wispforest.owo.ui.core.Color(owoColor.red(), owoColor.green(), owoColor.blue(), 1 - age / (float) maxAge);
        Color color = new Color(owoColor.argb(), true);
        //Renderer3d.renderThroughWalls();

        Renderer3d.renderFilled(new MatrixStack(), color, /*Color.YELLOW,*/ vec3d, new Vec3d(.25, .25, .25)/*, 100*/);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double x, double y, double z, double velX, double velY, double velZ) {
            return new TestParticle(clientWorld, x, y, z);
        }
    }
}
