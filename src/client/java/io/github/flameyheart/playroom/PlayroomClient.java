package io.github.flameyheart.playroom;

import com.chocohead.mm.api.ClassTinkerers;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import io.github.flameyheart.playroom.compat.ModOptional;
import io.github.flameyheart.playroom.config.ClientConfig;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.duck.client.ExpandedClientLoginNetworkHandler;
import io.github.flameyheart.playroom.event.LivingEntityEvents;
import io.github.flameyheart.playroom.freeze.CameraEntity;
import io.github.flameyheart.playroom.item.Aimable;
import io.github.flameyheart.playroom.mixin.EntityAccessor;
import io.github.flameyheart.playroom.registry.Entities;
import io.github.flameyheart.playroom.registry.Items;
import io.github.flameyheart.playroom.registry.Particles;
import io.github.flameyheart.playroom.render.entity.LaserProjectileRenderer;
import io.github.flameyheart.playroom.render.hud.HudRenderer;
import io.github.flameyheart.playroom.render.item.LaserGunRenderer;
import io.github.flameyheart.playroom.render.particle.TestParticle;
import io.github.flameyheart.playroom.render.screen.DonationListScreen;
import io.github.flameyheart.playroom.render.world.WorldRenderer;
import io.github.flameyheart.playroom.tiltify.Donation;
import io.github.flameyheart.playroom.toast.WarningToast;
import io.github.flameyheart.playroom.util.ClientUtils;
import me.x150.renderer.event.RenderEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib.animatable.client.RenderProvider;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PlayroomClient implements ClientModInitializer {
    public static final Map<Long, Double> animationStartTick = new HashMap<>();
    public static final BipedEntityModel.ArmPose LASER_GUN_POSE = ClassTinkerers.getEnum(BipedEntityModel.ArmPose.class, "LASER_GUN");
    public static final Map<UUID, Donation> DONATIONS = new LinkedHashMap<>();

    public static boolean orbitCameraEnabled = false;
    public static boolean forceOrbitCamera = false;
    public static boolean stopTicking = false;

    public static boolean wasFrozen = false;

    private final KeyBinding donationsScreenKeybind = ClientUtils.addKeybind("donations_screen", GLFW.GLFW_KEY_H);

    private final KeyBinding devKeybind1 = ClientUtils.addKeybind("dev1", GLFW.GLFW_KEY_F4);
    private final KeyBinding devKeybind2 = ClientUtils.addKeybind("dev2", GLFW.GLFW_KEY_F6);
    private final KeyBinding devKeybind3 = ClientUtils.addKeybind("dev3", GLFW.GLFW_KEY_F7);
    private final KeyBinding devKeybind4 = ClientUtils.addKeybind("dev4", GLFW.GLFW_KEY_F9);

    private final KeyBinding devKeybind5 = ClientUtils.addKeybind("dev5", GLFW.GLFW_KEY_UP);
    private final KeyBinding devKeybind6 = ClientUtils.addKeybind("dev6", GLFW.GLFW_KEY_DOWN);

    public static boolean isAiming(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof Aimable aimable && aimable.canAim(stack) && MinecraftClient.getInstance().options.attackKey.isPressed();
    }

    @Override
    public void onInitializeClient() {
        ClientConfig.INSTANCE.load();

        Items.LASER_GUN.setRenderer(new RenderProvider() {
            private final LaserGunRenderer renderer = new LaserGunRenderer();

            @Override
            public BuiltinModelItemRenderer getCustomRenderer() {
                return renderer;
            }
        });

        ParticleFactoryRegistry.getInstance().register(Particles.TEST_PARTICLE, TestParticle.Factory::new);
        EntityRendererRegistry.register(Entities.LASER_SHOT, LaserProjectileRenderer::new);

        registerEventListeners();
        handlePlayPackets();
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
        ClientUtils.listenKeybind(donationsScreenKeybind, (client) -> client.setScreen(new DonationListScreen()));

        ClientUtils.listenKeybind(devKeybind1, (client) -> CameraEntity.setCameraState(orbitCameraEnabled = !orbitCameraEnabled));
        ClientUtils.listenKeybind(devKeybind2, (client) -> forceOrbitCamera = !forceOrbitCamera);
        ClientUtils.listenKeybind(devKeybind3, (client) -> ClientPlayNetworking.send(Playroom.id("dev/freeze_player"), PacketByteBufs.create()));
        ClientUtils.listenKeybind(devKeybind4, (client) -> {
            ClientPlayNetworking.send(Playroom.id("dev/toggle_ticking"), PacketByteBufs.create());
            stopTicking = !stopTicking;
        });

        ClientUtils.listenKeybind(devKeybind5, (client) -> ClientPlayNetworking.send(Playroom.id("dev/freeze_player/add"), PacketByteBufs.create()));
        ClientUtils.listenKeybind(devKeybind6, (client) -> ClientPlayNetworking.send(Playroom.id("dev/freeze_player/rem"), PacketByteBufs.create()));

        RenderEvents.WORLD.register(WorldRenderer::render);
        RenderEvents.HUD.register(HudRenderer::render);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                ExpandedEntityData player = (ExpandedEntityData) client.player;
                boolean showIce = player.playroom$showIce();
                if (!showIce) {
                    wasFrozen = false;
                }
                if (!forceOrbitCamera) {
                    if (!orbitCameraEnabled && player.playroom$showIce()) {
                        CameraEntity.setCameraState(orbitCameraEnabled = true);
                        client.player.input.sneaking = false;
                    } else if (orbitCameraEnabled && !player.playroom$showIce()) {
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

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            Items.LASER_GUN.setShowAdvancedTooltip(Screen::hasShiftDown);
        });
    }

    private void handlePlayPackets() {
        ClientPlayNetworking.registerGlobalReceiver(Playroom.id("config/sync"), (client, handler, buf, responseSender) -> {
            String serverConfig = buf.readString();

            client.execute(() -> deserializeConfig(serverConfig));
        });

        ClientPlayNetworking.registerGlobalReceiver(Playroom.id("donation/add"), (client, handler, buf, responseSender) -> {
            UUID id = buf.readUuid();
            String donorName = buf.readString();
            String message = buf.readString();
            float amount = buf.readFloat();
            String currency = buf.readString();
            boolean autoApproved = buf.readBoolean();

            client.execute(() -> {
                DONATIONS.put(id, new Donation(id, donorName, message, amount, currency, autoApproved));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Playroom.id("donation/update"), (client, handler, buf, responseSender) -> {
            UUID id = buf.readUuid();
            Donation.Status status = buf.readEnumConstant(Donation.Status.class);

            client.execute(() -> {
                Donation donation = DONATIONS.get(id);
                if (donation != null) {
                    donation.updateStatus(status);
                } else {
                    Playroom.LOGGER.warn("Received donation update for unknown donation with id {}", id);
                }
            });
        });
    }

    private void handleLoginPackets() {
        ClientLoginNetworking.registerGlobalReceiver(Playroom.id("handshake"), (client, handler, buf, listenerAdder) -> {
            CompletableFuture<PacketByteBuf> future = new CompletableFuture<>();
            String serverConfig = buf.readString();

            client.execute(() -> {
                if (!deserializeConfig(serverConfig)) {
                    ((ExpandedClientLoginNetworkHandler) handler).playroom$disconnect(Text.translatable("playroom.multiplayer.disconnect.invalid_config"));
                    return;
                }

                PacketByteBuf byteBuf = PacketByteBufs.create();
                byteBuf.writeByte(Constants.PROTOCOL_VERSION);

                future.complete(byteBuf);
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

    public static boolean deserializeConfig(String data) {
        if (MinecraftClient.getInstance().isInSingleplayer()) {
            return true;
        }

        return Playroom.deserializeConfig(data);
    }
}
