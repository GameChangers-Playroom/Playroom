package io.github.flameyheart.playroom;

import com.chocohead.mm.api.ClassTinkerers;
import com.google.gson.Gson;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.ConfigField;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import io.github.flameyheart.playroom.compat.ModOptional;
import io.github.flameyheart.playroom.config.ClientConfig;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.config.TestCommand;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.duck.client.ExpandedClientLoginNetworkHandler;
import io.github.flameyheart.playroom.event.LivingEntityEvents;
import io.github.flameyheart.playroom.freeze.CameraEntity;
import io.github.flameyheart.playroom.item.Aimable;
import io.github.flameyheart.playroom.item.LaserGun;
import io.github.flameyheart.playroom.mixin.EntityAccessor;
import io.github.flameyheart.playroom.mixin.GsonConfigSerializerAccessor;
import io.github.flameyheart.playroom.registry.Items;
import io.github.flameyheart.playroom.registry.Particles;
import io.github.flameyheart.playroom.render.hud.HudRenderer;
import io.github.flameyheart.playroom.render.item.LaserGunRenderer;
import io.github.flameyheart.playroom.render.particle.TestParticle;
import io.github.flameyheart.playroom.render.world.WorldRenderer;
import io.github.flameyheart.playroom.toast.WarningToast;
import io.github.flameyheart.playroom.util.ClientUtils;
import me.x150.renderer.event.RenderEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.quiltmc.parsers.json.JsonReader;
import org.quiltmc.parsers.json.gson.GsonReader;
import software.bernie.geckolib.animatable.client.RenderProvider;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PlayroomClient implements ClientModInitializer {
    public static final Map<Long, Double> animationStartTick = new HashMap<>();
    public static final BipedEntityModel.ArmPose LASER_GUN_POSE = ClassTinkerers.getEnum(BipedEntityModel.ArmPose.class, "LASER_GUN");

    public static boolean orbitCameraEnabled = false;
    public static boolean forceOrbitCamera = false;
    public static float kbdInc = 0;
    public static Runnable kbd4Func = () -> {};
    public static Runnable kbd5Func = () -> {};

    private final KeyBinding devKeybind1 = ClientUtils.addKeybind("dev1", GLFW.GLFW_KEY_F4);
    private final KeyBinding devKeybind2 = ClientUtils.addKeybind("dev2", GLFW.GLFW_KEY_F6);
    private final KeyBinding devKeybind3 = ClientUtils.addKeybind("dev3", GLFW.GLFW_KEY_F7);
    private final KeyBinding devKeybind4 = ClientUtils.addKeybind("dev4", GLFW.GLFW_KEY_UP);
    private final KeyBinding devKeybind5 = ClientUtils.addKeybind("dev5", GLFW.GLFW_KEY_DOWN);

    public static boolean isAiming(Item item) {
        return item instanceof Aimable && MinecraftClient.getInstance().options.attackKey.isPressed();
    }

    @Override
    public void onInitializeClient() {
        ClientConfig.INSTANCE.serializer().load();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> TestCommand.register(dispatcher));
        Items.LASER_GUN.setRenderer(new RenderProvider() {
            private final LaserGunRenderer renderer = new LaserGunRenderer();

            @Override
            public BuiltinModelItemRenderer getCustomRenderer() {
                return renderer;
            }
        });

        ParticleFactoryRegistry.getInstance().register(Particles.TEST_PARTICLE, TestParticle.Factory::new);

        registerEventListeners();
        handleLoginPackets();

        ModOptional.ifPresent("tweakeroo", () -> {
            KeybindMulti keybind = (KeybindMulti) FeatureToggle.TWEAK_FREE_CAMERA.getKeybind();
            IHotkeyCallback callback = keybind.getCallback();
            keybind.setCallback((action, key) -> {
                if (orbitCameraEnabled) return false;
                return callback.onKeyAction(action, key);
            });
        });
    }

    private void registerEventListeners() {
        ClientUtils.listenKeybind(devKeybind1, (client) -> CameraEntity.setCameraState(orbitCameraEnabled = !orbitCameraEnabled));
        ClientUtils.listenKeybind(devKeybind2, (client) -> forceOrbitCamera = !forceOrbitCamera);
        ClientUtils.listenKeybind(devKeybind3, (client) -> ClientPlayNetworking.send(Playroom.id("dev/freeze_player"), PacketByteBufs.create()));
        ClientUtils.listenKeybind(devKeybind4, (client) -> {
            kbd4Func.run();
            ClientConfig.INSTANCE.serializer().save();
        });
        ClientUtils.listenKeybind(devKeybind5, (client) -> {
            kbd5Func.run();
            ClientConfig.INSTANCE.serializer().save();
        });

        RenderEvents.WORLD.register(WorldRenderer::render);
        RenderEvents.HUD.register(HudRenderer::render);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                if (!forceOrbitCamera) {
                    if (!orbitCameraEnabled && ((ExpandedEntityData) client.player).playroom$getGunFreezeTicks() > 0) {
                        CameraEntity.setCameraState(orbitCameraEnabled = true);
                        client.player.input.sneaking = false;
                    } else if (orbitCameraEnabled && ((ExpandedEntityData) client.player).playroom$getGunFreezeTicks() <= 0) {
                        CameraEntity.setCameraState(orbitCameraEnabled = false);
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
            CameraEntity.setCameraState(orbitCameraEnabled = false);
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

    private void handleLoginPackets() {

        ClientLoginNetworking.registerGlobalReceiver(Playroom.id("handshake"), (client, handler, buf, listenerAdder) -> {
            CompletableFuture<PacketByteBuf> future = new CompletableFuture<>();
            String serverConfig = buf.readString();

            client.execute(() -> {
                ConfigClassHandler<ServerConfig> config = ServerConfig.INSTANCE;
                Gson gson = ((GsonConfigSerializerAccessor) config.serializer()).getGson();
                try (JsonReader jsonReader = JsonReader.json5(serverConfig)) {

                    GsonReader gsonReader = new GsonReader(jsonReader);

                    Map<String, ConfigField<?>> fieldMap = Arrays.stream(config.fields())
                        .filter(field -> field.serial().isPresent())
                        .collect(Collectors.toMap(f -> f.serial().orElseThrow().serialName(), Function.identity()));

                    jsonReader.beginObject();

                    while (jsonReader.hasNext()) {
                        String name = jsonReader.nextName();
                        ConfigField<?> field = fieldMap.get(name);
                        if (field == null) {
                            Playroom.LOGGER.error("Unknown config field '{}' sent from server!", name);
                            ((ExpandedClientLoginNetworkHandler) handler).playroom$disconnect(Text.translatable("playroom.multiplayer.disconnect.invalid_config"));
                            jsonReader.skipValue();
                            return;
                        }

                        try {
                            field.access().set(gson.fromJson(gsonReader, field.access().type()));
                        } catch (Exception e) {
                            Playroom.LOGGER.error("Failed to deserialize config field '{}'.", name, e);
                            jsonReader.skipValue();
                        }
                    }

                    jsonReader.endObject();

                    PacketByteBuf byteBuf = PacketByteBufs.create();
                    byteBuf.writeByte(Constants.PROTOCOL_VERSION);

                    future.complete(byteBuf);
                } catch (IOException e) {
                    Playroom.LOGGER.error("Failed to decode server config!", e);
                    ((ExpandedClientLoginNetworkHandler) handler).playroom$disconnect(Text.translatable("playroom.multiplayer.disconnect.invalid_config"));
                }
            });
            return future;
        });

        ClientLoginNetworking.registerGlobalReceiver(Playroom.id("warning/mismatch/protocol"), (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                client.getToastManager().add(new WarningToast(Text.translatable("playroom.warning.protocol.title"), Text.translatable("playroom.warning.protocol.message")));
            });

            return CompletableFuture.completedFuture(PacketByteBufs.create());
        });
    }
}
