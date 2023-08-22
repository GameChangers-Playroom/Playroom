package io.github.flameyheart.playroom;

import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import io.github.flameyheart.playroom.compat.ModOptional;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.event.LivingEntityEvents;
import io.github.flameyheart.playroom.freeze.CameraEntity;
import io.github.flameyheart.playroom.mixin.EntityAccessor;
import io.github.flameyheart.playroom.registry.Particles;
import io.github.flameyheart.playroom.render.hud.HudRenderer;
import io.github.flameyheart.playroom.render.particle.TestParticle;
import io.github.flameyheart.playroom.render.world.WorldRenderer;
import io.github.flameyheart.playroom.util.ClientUtils;
import me.x150.renderer.event.RenderEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

public class PlayroomClient implements ClientModInitializer {
    public static boolean cameraEnabled = false;
    public static boolean forceCamera = false;

    private final KeyBinding devKeybind1 = ClientUtils.addKeybind("dev1", GLFW.GLFW_KEY_F4);
    private final KeyBinding devKeybind2 = ClientUtils.addKeybind("dev2", GLFW.GLFW_KEY_F6);
    private final KeyBinding devKeybind3 = ClientUtils.addKeybind("dev3", GLFW.GLFW_KEY_F7);

    @Override
    public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(Particles.TEST_PARTICLE, TestParticle.Factory::new);

        RenderEvents.WORLD.register(WorldRenderer::render);
        RenderEvents.HUD.register(HudRenderer::render);

        ClientUtils.listenKeybind(devKeybind1, (client) -> CameraEntity.setCameraState(cameraEnabled = !cameraEnabled));
        ClientUtils.listenKeybind(devKeybind2, (client) -> forceCamera = !forceCamera);
        ClientUtils.listenKeybind(devKeybind3, (client) -> ClientPlayNetworking.send(Playroom.id("dev/freeze_player"), PacketByteBufs.create()));

        ModOptional.ifPresent("tweakeroo", () -> {
            KeybindMulti keybind = (KeybindMulti) FeatureToggle.TWEAK_FREE_CAMERA.getKeybind();
            IHotkeyCallback callback = keybind.getCallback();
            keybind.setCallback((action, key) -> {
                if (cameraEnabled) return false;
                return callback.onKeyAction(action, key);
            });
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                if (!forceCamera) {
                    if (!cameraEnabled && ((ExpandedEntityData) client.player).playroom$getGunFreezeTicks() > 0) {
                        CameraEntity.setCameraState(cameraEnabled = true);
                        client.player.input.sneaking = false;
                    } else if (cameraEnabled && ((ExpandedEntityData) client.player).playroom$getGunFreezeTicks() <= 0) {
                        CameraEntity.setCameraState(cameraEnabled = false);
                    }
                }

                CameraEntity.movementTick();
            }
        });

        LivingEntityEvents.END_TRAVEL.register(baseEntity -> {
            if (baseEntity instanceof PlayerEntity player) {
                player.getWorld().getProfiler().push("playroom_freezing");
                ExpandedEntityData eEntity = (ExpandedEntityData) player;

                if (player.getWorld().isClient && !player.isDead() && eEntity.playroom$isFrozen() && !player.isOnGround()) {
                    player.setVelocity(player.getVelocity().multiply(0.8, 1, 0.8));
                    ((EntityAccessor) player).callScheduleVelocityUpdate();
                }
                player.getWorld().getProfiler().pop();
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            CameraEntity.setCameraState(cameraEnabled = false);
        });

        //Test code for rendering a texture that always faces the camera
        /*WorldRenderEvents.END.register(context -> {
            Camera camera = context.camera();
            float tickDelta = context.tickDelta();

            Vec3d targetPosition = new Vec3d(0, 152, 0);
            Vec3d targetPosition2 = new Vec3d(0.5, 152.5, 0.5);
            Vec3d transformedPosition = targetPosition.subtract(camera.getPos());

            MatrixStack matrixStack = new MatrixStack();
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
            matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);
            matrixStack.translate(0.5, 0.5, 0.5);

            Vec3d vec3d = camera.getPos();
            double d = targetPosition2.x - vec3d.x;
            double e = targetPosition2.y - vec3d.y;
            double f = targetPosition2.z - vec3d.z;
            double g = Math.sqrt(d * d + f * f);
            float h = MathHelper.wrapDegrees((float) (-(MathHelper.atan2(e, g) * 57.2957763671875)));
            float i = MathHelper.wrapDegrees((float) (MathHelper.atan2(f, d) * 57.2957763671875) - 90.0f);

            Quaternionf quaternionf = new Quaternionf(camera.getRotation());
            quaternionf.rotationYXZ(-i * ((float) Math.PI / 180), h * ((float) Math.PI / 180), 0.0f);
            quaternionf.rotateZ(MathHelper.lerp(tickDelta, 0, 0));

            matrixStack.multiply(quaternionf);
            matrixStack.translate(-0.5, -0.5, -0.5);

            Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
            buffer.vertex(positionMatrix, 0, 1, 0).color(1f, 1f, 1f, 1f).texture(0f, 0f).next();
            buffer.vertex(positionMatrix, 0, 0, 0).color(1f, 1f, 1f, 1f).texture(0f, 1f).next();
            buffer.vertex(positionMatrix, 1, 0, 0).color(1f, 1f, 1f, 1f).texture(1f, 1f).next();
            buffer.vertex(positionMatrix, 1, 1, 0).color(1f, 1f, 1f, 1f).texture(1f, 0f).next();

            RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
            RenderSystem.setShaderTexture(0, Playroom.id("textures/item/gradient.png"));
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.disableCull();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();

            tessellator.draw();

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
        });*/
    }
}
