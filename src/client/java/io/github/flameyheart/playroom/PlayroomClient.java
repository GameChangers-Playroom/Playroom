package io.github.flameyheart.playroom;

import com.chocohead.mm.api.ClassTinkerers;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import io.github.flameyheart.playroom.compat.ModOptional;
import io.github.flameyheart.playroom.config.ClientConfig;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.duck.PlayerDisplayName;
import io.github.flameyheart.playroom.duck.client.ExpandedClientLoginNetworkHandler;
import io.github.flameyheart.playroom.event.LivingEntityEvents;
import io.github.flameyheart.playroom.freeze.CameraEntity;
import io.github.flameyheart.playroom.item.Aimable;
import io.github.flameyheart.playroom.mixin.EntityAccessor;
import io.github.flameyheart.playroom.registry.Entities;
import io.github.flameyheart.playroom.registry.Items;
import io.github.flameyheart.playroom.registry.Particles;
import io.github.flameyheart.playroom.render.entity.LaserProjectileRenderer;
import io.github.flameyheart.playroom.render.entity.ModelPosition;
import io.github.flameyheart.playroom.render.hud.HudRenderer;
import io.github.flameyheart.playroom.render.item.LaserGunRenderer;
import io.github.flameyheart.playroom.render.particle.TestParticle;
import io.github.flameyheart.playroom.render.screen.DonationListScreen;
import io.github.flameyheart.playroom.render.world.WorldRenderer;
import io.github.flameyheart.playroom.tiltify.Donation;
import io.github.flameyheart.playroom.toast.WarningToast;
import io.github.flameyheart.playroom.util.ClientUtils;
import io.github.flameyheart.playroom.zoom.ZoomHelper;
import io.github.flameyheart.playroom.zoom.interpolate.SmoothInterpolator;
import io.github.flameyheart.playroom.zoom.interpolate.TransitionInterpolator;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.animatable.client.RenderProvider;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PlayroomClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Playroom Client");
    private static final KeyBinding DONATIONS_SCREEN_KEYBIND = ClientUtils.addKeybind("donations_screen", GLFW.GLFW_KEY_H);

    public static final Map<Long, Double> ANIMATION_START_TICK = new HashMap<>();
    public static final BipedEntityModel.ArmPose LASER_GUN_POSE = ClassTinkerers.getEnum(BipedEntityModel.ArmPose.class, "LASER_GUN");
    public static final Map<UUID, Donation> DONATIONS = new LinkedHashMap<>();
    public static final int MAX_SCROLL_TIERS = 5;
    public static final Map<ExpandedEntityData, Map<String, ModelPosition>> FROZEN_MODEL = new HashMap<>();

    public static boolean orbitCameraEnabled = false;

    private static boolean zooming = false;
    private static final ZoomHelper ZOOM_HELPER = new ZoomHelper(
      new TransitionInterpolator(
        () -> ClientConfig.instance().laserAimZoomInTransition,
        () -> ClientConfig.instance().laserAimZoomOutTransition,
        () -> ClientConfig.instance().laserAimZoomInTime,
        () -> ClientConfig.instance().laserAimZoomOutTime
      ),
      new SmoothInterpolator(() -> MathHelper.lerp(ClientConfig.defaults().laserAimCameraSmoothness / 100.0, 1.0, 0.1)),
      () -> ServerConfig.instance().laserAimZoom,
      () -> 3,
      () -> MAX_SCROLL_TIERS,
      () -> true
    );
    private static double previousZoomDivisor;

    @Override
    public void onInitializeClient() {
        ClientConfig.INSTANCE.load();

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
        ClientUtils.listenKeybind(DONATIONS_SCREEN_KEYBIND, (client) -> client.setScreen(new DonationListScreen()));

        RenderEvents.WORLD.register(WorldRenderer::render);
        RenderEvents.HUD.register(HudRenderer::renderDebugInfo);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ZOOM_HELPER.tick(isZooming(), 0);
            if (client.player != null) {
                if (Playroom.getServer() == null) Playroom.serverTime++;
                ExpandedEntityData player = (ExpandedEntityData) client.player;
                if (!orbitCameraEnabled && player.playroom$showIce()) {
                    CameraEntity.setCameraState(orbitCameraEnabled = true);
                    client.player.input.sneaking = false;
                } else if (orbitCameraEnabled && !player.playroom$showIce()) {
                    CameraEntity.setCameraState(orbitCameraEnabled = false);
                }

                CameraEntity.movementTick();
            }
        });
        LivingEntityEvents.END_TRAVEL.register(baseEntity -> {
            if (baseEntity instanceof PlayerEntity player) {
                player.getWorld().getProfiler().push("playroom_freezing");
                ExpandedEntityData eEntity = (ExpandedEntityData) player;

                if (player.getWorld().isClient && !player.isDead() && eEntity.playroom$isFrozen()) {
                    if (eEntity.playroom$showIce() && !player.isOnGround()) {
                        player.setVelocity(player.getVelocity().multiply(0.8, 1, 0.8));
                        ((EntityAccessor) player).callScheduleVelocityUpdate();
                    }
                }

                player.getWorld().getProfiler().pop();
            }
        });
        LivingEntityEvents.END_BASE_TICK.register(entity -> {
            ExpandedEntityData eEntity = (ExpandedEntityData) entity;
            boolean showIce = eEntity.playroom$showIce();
            if (!showIce) {
                FROZEN_MODEL.remove(entity);
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            CameraEntity.setCameraState(orbitCameraEnabled = false);
            Playroom.serverTime = 0;
        });
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            Items.LASER_GUN.setShowAdvancedTooltip(Screen::hasShiftDown);
            Items.LASER_GUN.setRenderer(new RenderProvider() {
                private final LaserGunRenderer renderer = new LaserGunRenderer();

                @Override
                public BuiltinModelItemRenderer getCustomRenderer() {
                    return renderer;
                }
            });
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

        ClientPlayNetworking.registerGlobalReceiver(Playroom.id("player_name"), (client, handler, buf, responseSender) -> {
            List<PlayerDisplayName> displayNames = buf.readList(packetByteBuf -> {
                UUID player = packetByteBuf.readUuid();
                Text prefix = packetByteBuf.readText();
                Text name = packetByteBuf.readText();

                return new PlayerDisplayName(player, prefix, name);
            });

            client.execute(() -> {
                for (PlayerDisplayName displayName : displayNames) {
                    PlayerEntity player = client.world.getPlayerByUuid(displayName.player());
                    if (player != null) {
                        ((ExpandedEntityData) player).playroom$setDisplayName(displayName.prefix(), displayName.displayName());
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Playroom.id("time_sync"), (client, handler, buf, responseSender) -> {
            long serverTime = buf.readLong();

            client.execute(() -> {
                if (serverTime - 5 > Playroom.serverTime) {
                    Playroom.LOGGER.warn("Server time is {} ticks ahead of client", serverTime - Playroom.serverTime);
                } else if (serverTime < Playroom.serverTime - 5) {
                    Playroom.LOGGER.warn("Server time is {} ticks behind client", Playroom.serverTime - serverTime);
                }
                Playroom.serverTime = serverTime;
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

        LOGGER.info("Received {} bytes from server", data.getBytes().length);

        return Playroom.deserializeConfig(data);
    }

    public static boolean isAiming(ItemStack stack) {
        Item item = stack.getItem();
        boolean aiming = item instanceof Aimable aimable && aimable.canAim(stack) && MinecraftClient.getInstance().options.attackKey.isPressed();
        if (zooming != aiming) {
            setZooming(aiming);
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(aiming);
            ClientPlayNetworking.send(Playroom.id("aiming"), buf);
        }
        return aiming;
    }

    private static void setZooming(boolean zooming) {
        PlayroomClient.zooming = zooming;
    }

    public static boolean isZooming() {
        return PlayroomClient.zooming;
    }

    public static double getPreviousZoomDivisor() {
        return previousZoomDivisor;
    }

    public static double getZoomDivisor(float tickDelta) {
        if (!zooming) {
            ZOOM_HELPER.reset();
        }

        double zoomDivisor = ZOOM_HELPER.getZoomDivisor(tickDelta);
        previousZoomDivisor = zoomDivisor;
        return zoomDivisor;
    }
}
