package io.github.flameyheart.playroom;

import com.chocohead.mm.api.ClassTinkerers;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import io.github.flameyheart.playroom.compat.ModOptional;
import io.github.flameyheart.playroom.config.ClientConfig;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.dontation.RewardDisplayer;
import io.github.flameyheart.playroom.duck.FreezableEntity;
import io.github.flameyheart.playroom.duck.PlayerDisplayName;
import io.github.flameyheart.playroom.duck.client.ExpandedClientLoginNetworkHandler;
import io.github.flameyheart.playroom.duck.client.FancyDisplayName;
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
import io.github.flameyheart.playroom.zoom.TransitionType;
import io.github.flameyheart.playroom.zoom.ZoomHelper;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.animatable.client.RenderProvider;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PlayroomClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Playroom Client");
    private static final KeyBinding DONATIONS_SCREEN_KEYBIND = ClientUtils.addKeybind("donations_screen", GLFW.GLFW_KEY_H);
    private static final KeyBinding SWAP_MODE_KEYBIND = ClientUtils.addKeybind("swap_mode", GLFW.GLFW_KEY_R);

    public static final Map<Long, Double> ANIMATION_START_TICK = new HashMap<>();
    public static final BipedEntityModel.ArmPose LASER_GUN_POSE = ClassTinkerers.getEnum(BipedEntityModel.ArmPose.class, "LASER_GUN");
    public static final Map<UUID, Donation> DONATIONS = new LinkedHashMap<>();
    public static final Map<FreezableEntity, Map<String, ModelPosition>> FROZEN_MODEL = new HashMap<>();

    public static boolean orbitCameraEnabled = false;

    private static boolean aimZoom = false;
    private static boolean unfreezeZoom = false;
    private static final ZoomHelper AIM_ZOOM = new ZoomHelper(
      new TransitionInterpolator(
        () -> ClientConfig.instance().laserAimZoomInTransition,
        () -> ClientConfig.instance().laserAimZoomOutTransition.opposite(),
        () -> ClientConfig.instance().laserAimZoomInTime,
        () -> ClientConfig.instance().laserAimZoomOutTime
      ),
      () -> ServerConfig.instance().laserAimZoom
    );
    public static final ZoomHelper UNFREEZE_ZOOM = new ZoomHelper(
      new TransitionInterpolator(
        () -> ClientConfig.instance().freezeZoomTransition,
        () -> TransitionType.INSTANT,
        () -> ClientConfig.instance().freezeZoomDuration,
        () -> 0.
      ),
      () -> ClientConfig.instance().freezeZoomTarget
    );
    private static double previousAimZoomDivisor;
    private static double previousUnfreezeZoomDivisor;

    private static final RewardDisplayer displayer = new RewardDisplayer();

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
        ClientUtils.listenKeybind(DONATIONS_SCREEN_KEYBIND, (client) -> {
            if (client.getOverlay() != null) return;
            client.setScreen(new DonationListScreen());
        });
        ClientUtils.listenKeybind(SWAP_MODE_KEYBIND, (client) -> sendPacket("swap_mode", PacketByteBufs.empty()));

        RenderEvents.WORLD.register(WorldRenderer::render);
        RenderEvents.HUD.register(HudRenderer::renderDebugInfo);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            AIM_ZOOM.tick(hasAimZoom());
            UNFREEZE_ZOOM.tick(hasUnfreezeZoom());
            if (client.player != null) {
                if (Playroom.getServer() == null) Playroom.serverTime++;
                FreezableEntity player = (FreezableEntity) client.player;
                if (!orbitCameraEnabled && player.playroom$isFrozen()) {
                    CameraEntity.setCameraState(orbitCameraEnabled = true);
                    client.player.input.sneaking = false;
                } else if (orbitCameraEnabled && !player.playroom$isFrozen()) {
                    CameraEntity.setCameraState(orbitCameraEnabled = false);
                }

                CameraEntity.movementTick();
            }
        });
        LivingEntityEvents.END_TRAVEL.register(baseEntity -> {
            if (baseEntity instanceof PlayerEntity player) {
                player.getWorld().getProfiler().push("playroom_freezing");
                FreezableEntity entity = (FreezableEntity) player;

                if (player.getWorld().isClient && !player.isDead() && entity.playroom$isFrozen() && !player.isOnGround()) {
                    player.setVelocity(player.getVelocity().multiply(0.8, 1, 0.8));
                    ((EntityAccessor) player).callScheduleVelocityUpdate();
                }

                player.getWorld().getProfiler().pop();
            }
        });
        LivingEntityEvents.END_BASE_TICK.register(entity -> {
            FreezableEntity eEntity = (FreezableEntity) entity;
            boolean showIce = eEntity.playroom$isFrozen();
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
        ClientPlayNetworking.registerGlobalReceiver(Playroom.id("donation"), (client, handler, buf, responseSender) -> {
            Donation donation = buf.decode(NbtOps.INSTANCE, Donation.CODEC);

            client.execute(() -> {
                DONATIONS.put(donation.id(), donation);
                if (donation.status() != Donation.Status.NORMAL) return;

                displayer.displayDonation(donation, ClientConfig.instance().donationExpiryTime);
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
                if (client.world == null) return;
                for (PlayerDisplayName displayName : displayNames) {
                    PlayerEntity player = client.world.getPlayerByUuid(displayName.player());
                    if (player instanceof FancyDisplayName fancyPlayer) {
                        fancyPlayer.playroom$setDisplayName(displayName.displayName());
                        fancyPlayer.playroom$setPrefix(displayName.prefix());
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
        ClientPlayNetworking.registerGlobalReceiver(Playroom.id("experiment"), (client, handler, buf, responseSender) -> {
            String experiment = buf.readString();
            boolean status = buf.readBoolean();

            client.execute(() -> {
                if (Playroom.getServer() != null) return;
                Playroom.setExperimentStatus(experiment, status);
            });
        });
    }

    private void handleLoginPackets() {
        ClientLoginNetworking.registerGlobalReceiver(Playroom.id("handshake"), (client, handler, buf, listenerAdder) -> {
            CompletableFuture<PacketByteBuf> future = new CompletableFuture<>();
            String serverConfig = buf.readString();
            long serverTime = buf.readLong();

            client.execute(() -> {
                Playroom.serverTime = serverTime;
                if (!deserializeConfig(serverConfig)) {
                    ((ExpandedClientLoginNetworkHandler) handler).playroom$disconnect(Text.translatable("playroom.multiplayer.disconnect.invalid_config"));
                    return;
                }

                PacketByteBuf byteBuf = PacketByteBufs.create();
                byteBuf.writeByte(Constants.PROTOCOL_VERSION);
                byteBuf.writeString(Playroom.getModVersion());

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

        ClientLoginNetworking.registerGlobalReceiver(Playroom.id("warning/mismatch/version"), (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                client.getToastManager().add(new WarningToast(Text.translatable("playroom.warning.version.title"), Text.translatable("playroom.warning.version.message")));
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

    public static void setUnfreezeZoom(boolean zooming) {
        PlayroomClient.unfreezeZoom = zooming;
    }

    public static boolean hasUnfreezeZoom() {
        return PlayroomClient.unfreezeZoom;
    }

    public static double getUnfreezeZoomDivisor(float tickDelta) {
        double zoomDivisor = UNFREEZE_ZOOM.getZoomDivisor(tickDelta);
        previousUnfreezeZoomDivisor = zoomDivisor;
        return zoomDivisor;
    }

    public static double getPreviousUnfreezeZoomDivisor() {
        return previousUnfreezeZoomDivisor;
    }

    public static boolean isAiming(ItemStack stack) {
        Item item = stack.getItem();
        boolean aiming = item instanceof Aimable aimable && aimable.canAim(stack) && MinecraftClient.getInstance().options.attackKey.isPressed();
        if (aimZoom != aiming) {
            setAimZoom(aiming);
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(aiming);
            ClientPlayNetworking.send(Playroom.id("aiming"), buf);
        }
        return aiming;
    }

    private static void setAimZoom(boolean zooming) {
        PlayroomClient.aimZoom = zooming;
    }

    public static boolean hasAimZoom() {
        return PlayroomClient.aimZoom;
    }

    public static double getPreviousAimZoomDivisor() {
        return previousAimZoomDivisor;
    }

    public static double getAimZoomDivisor(float tickDelta) {
        if (!aimZoom) {
            AIM_ZOOM.reset();
        }

        double zoomDivisor = AIM_ZOOM.getZoomDivisor(tickDelta);
        previousAimZoomDivisor = zoomDivisor;
        return zoomDivisor;
    }

    public static void sendPacket(String id, PacketByteBuf buf) {
        ClientPlayNetworking.send(Playroom.id(id), buf);
    }

}
