package io.github.flameyheart.playroom.freeze;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import io.github.flameyheart.playroom.compat.ModOptional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class CameraEntity extends ClientPlayerEntity {
    @Nullable
    private static CameraEntity camera;
    @Nullable
    private static Entity originalCameraEntity;
    private static boolean cullChunksOriginal;
    private static boolean originalCameraWasPlayer;

    private CameraEntity(MinecraftClient mc, ClientWorld world,
                         ClientPlayNetworkHandler netHandler, StatHandler stats,
                         ClientRecipeBook recipeBook) {
        super(mc, world, netHandler, stats, recipeBook, false, false);
    }

    @Override
    public boolean isSpectator() {
        return true;
    }

    public static void movementTick() {
        CameraEntity camera = getCamera();

        if (camera != null) {

            camera.updateLastTickPosition();

            camera.handleMotion();
        }
    }

    private void handleMotion() {
        this.setVelocity(client.player.getVelocity());
        if (client.player.isOnGround()) {
            this.setVelocity(this.getVelocity().multiply(1.0, 0.0, 1.0));
        }
        this.move(MovementType.SELF, this.getVelocity());
        this.setPosition(client.player.getPos());
    }

    private void updateLastTickPosition() {
        this.lastRenderX = this.getX();
        this.lastRenderY = this.getY();
        this.lastRenderZ = this.getZ();

        this.prevX = this.getX();
        this.prevY = this.getY();
        this.prevZ = this.getZ();

        this.prevYaw = this.getYaw();
        this.prevPitch = this.getPitch();

        this.prevHeadYaw = this.headYaw;
    }

    public void setCameraRotations(float yaw, float pitch) {
        this.setYaw(yaw);
        this.setPitch(pitch);

        this.headYaw = yaw;
    }

    public void updateCameraRotations(float yawChange, float pitchChange) {
        float yaw = this.getYaw() + yawChange * 0.15F;
        float pitch = MathHelper.clamp(this.getPitch() + pitchChange * 0.15F, -90F, 90F);

        this.setYaw(yaw);
        this.setPitch(pitch);

        this.setCameraRotations(yaw, pitch);
    }

    private static CameraEntity createCameraEntity(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        CameraEntity camera = new CameraEntity(mc, mc.world, player.networkHandler, player.getStatHandler(), player.getRecipeBook());
        camera.noClip = true;
        float yaw = player.getYaw();
        float pitch = player.getPitch();

        camera.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), yaw, pitch);
        camera.setRotation(yaw, pitch);

        return camera;
    }

    @Nullable
    public static CameraEntity getCamera() {
        return camera;
    }

    public static void setCameraState(boolean enabled) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world != null && client.player != null) {
            if (enabled) {
                ModOptional.ifPresent("tweakeroo", () -> {
                    FeatureToggle.TWEAK_FREE_CAMERA.setBooleanValue(false);
                });
                createAndSetCamera(client);
            } else {
                removeCamera(client);
            }
        }
    }

    public static boolean originalCameraWasPlayer() {
        return originalCameraWasPlayer;
    }

    private static void createAndSetCamera(MinecraftClient mc) {
        camera = createCameraEntity(mc);
        originalCameraEntity = mc.getCameraEntity();
        originalCameraWasPlayer = originalCameraEntity == mc.player;
        cullChunksOriginal = mc.chunkCullingEnabled;

        mc.setCameraEntity(camera);
        //mc.chunkCullingEnabled = false; // Disable chunk culling
    }

    private static void removeCamera(MinecraftClient mc) {
        if (mc.world != null && camera != null) {
            // Re-fetch the player entity, in case the player died while in Free Camera mode and the instance changed
            mc.setCameraEntity(originalCameraWasPlayer ? mc.player : originalCameraEntity);
            mc.chunkCullingEnabled = cullChunksOriginal;
        }

        originalCameraEntity = null;
        camera = null;
    }
}
