package io.github.flameyheart.playroom.render.item;

import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.compat.ModOptional;
import io.github.flameyheart.playroom.config.ClientConfig;
import io.github.flameyheart.playroom.item.LaserGun;
import io.github.flameyheart.playroom.item.OldLaserGun;
import io.github.flameyheart.playroom.mixin.compat.geo.AnimationControllerAccessor;
import io.github.flameyheart.playroom.render.hud.HudRenderer;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.texture.AnimatableTexture;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.util.RenderUtils;

import java.util.Objects;

public class LaserGunRenderer extends GeoItemRenderer<LaserGun> {

    private static final boolean IS_IRIS_PRESENT = ModOptional.isPresent("iris");

    private final LaserGunModel model;
    private final MinecraftClient client = MinecraftClient.getInstance();

    private int animFrameTick;
    private int chargeLevel;
    private float alphaMulti;

    private VertexConsumerProvider bufferSource;
    private ModelTransformationMode transformType;

    public LaserGunRenderer() {

        super(new LaserGunModel());

        model = (LaserGunModel) getGeoModel();

        addRenderLayer(new EnergyLayer(this));
        addRenderLayer(new ChargeLayer(this));

    }

    public String getLayerNameByState() {
        return animatable == null ? LaserGunModel.ENABLED_RANGEMODE : animatable.isRapidFire(getCurrentItemStack()) ? LaserGunModel.ENABLED_RAPIDFIREMODE : LaserGunModel.ENABLED_RANGEMODE;
    }

    /**
     * TODO: Add animation callback
     */
    @Override
    public void render(ItemStack stack, ModelTransformationMode transformType, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
        this.bufferSource = bufferSource;
        this.transformType = transformType;
        boolean isFirstPerson = transformType.isFirstPerson();

        currentItemStack = stack;
        animatable = (LaserGun) stack.getItem();
        alphaMulti = isFirstPerson ? (IS_IRIS_PRESENT && IrisApi.getInstance().isShaderPackInUse() ? .3f : 1f) : 1;

        if (animatable.isRapidFire(stack)) {

            AnimationController<GeoAnimatable> controller = animatable.getAnimationController(stack);
            if (controller != null && ((AnimationControllerAccessor) controller).getTriggeredAnimation() == null) {
                controller.tryTriggerAnimation("rapidfire_mode");
            }

        }

        chargeLevel = animatable.getPlayroomTag(stack).getInt("Charge");

        super.render(stack, transformType, poseStack, bufferSource, transformType == ModelTransformationMode.GUI ? LightmapTextureManager.MAX_LIGHT_COORDINATE : packedLight, 0);

        if (!isFirstPerson) return;

        AbstractClientPlayerEntity player = client.player;
        Identifier playerSkin = player.getSkinTexture();
        VertexConsumer armTexture = bufferSource.getBuffer(RenderLayer.getEntitySolid(playerSkin));
        VertexConsumer sleeveTexture = bufferSource.getBuffer(RenderLayer.getEntityTranslucent(playerSkin));

        PlayerEntityRenderer playerEntityRenderer = (PlayerEntityRenderer) client.getEntityRenderDispatcher().getRenderer(player);
        PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = playerEntityRenderer.getModel();
        float scale = .6666f;

        ModelTransform leftSleeveTransform = playerEntityModel.leftSleeve.getTransform();
        ModelTransform rightSleeveTransform = playerEntityModel.rightSleeve.getTransform();

        if (ClientConfig.instance().laserGunHandRender == ClientConfig.LaserGunHandRender.MAC) {
            poseStack.push();
            poseStack.scale(scale, scale, scale);

            if (player.getModel().equals("default")) {
                if (player.getMainArm() == Arm.RIGHT) {
                    playerEntityModel.leftArm.setPivot(2.2512f, 11.5f, 24.251f);
                    playerEntityModel.leftArm.setAngles(1.6707f, 2.356f, 0);
                    playerEntityModel.rightArm.setPivot(12.2512f, 11.5f, 24.251f);
                    playerEntityModel.rightArm.setAngles(1.5707f, 3.142f, 0);
                } else if (player.getMainArm() == Arm.LEFT) {
                    playerEntityModel.leftArm.setPivot(10.2512f, 11.5f, 24.251f);
                    playerEntityModel.leftArm.setAngles(1.5707f, 3.142f, 0);
                    playerEntityModel.rightArm.setPivot(20.1952f, 11.5f, 23.261f);
                    playerEntityModel.rightArm.setAngles(1.5707f, 3.741f, 0);
                }
            } else if (player.getModel().equals("slim")) {
                if (player.getMainArm() == Arm.RIGHT) {
                    playerEntityModel.leftArm.setPivot(2.7512f, 11.5f, 24.251f);
                    playerEntityModel.leftArm.setAngles(1.6707f, 2.356f, 0);
                    playerEntityModel.rightArm.setPivot(12.7512f, 11.5f, 24.251f);
                    playerEntityModel.rightArm.setAngles(1.5707f, 3.142f, 0);
                } else if (player.getMainArm() == Arm.LEFT) {
                    playerEntityModel.leftArm.setPivot(9.7512f, 11.5f, 24.301f);
                    playerEntityModel.leftArm.setAngles(1.5707f, 3.142f, 0);
                    playerEntityModel.rightArm.setPivot(19.6847f, 11.5f, 23.338f);
                    playerEntityModel.rightArm.setAngles(1.5707f, 3.739f, 0);
                }
            }

            playerEntityModel.leftSleeve.copyTransform(playerEntityModel.leftArm);
            playerEntityModel.rightSleeve.copyTransform(playerEntityModel.rightArm);

            int playerOverlay = LivingEntityRenderer.getOverlay(player, 0);

            playerEntityModel.leftArm.render(poseStack, armTexture, packedLight, playerOverlay);
            playerEntityModel.leftSleeve.render(poseStack, sleeveTexture, packedLight, playerOverlay);
            playerEntityModel.rightArm.render(poseStack, armTexture, packedLight, playerOverlay);
            playerEntityModel.rightSleeve.render(poseStack, sleeveTexture, packedLight, playerOverlay);
        }
    }

    @Override
    public void renderRecursively(MatrixStack matrixStack, LaserGun animatable, GeoBone bone, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        MinecraftClient client = MinecraftClient.getInstance();

        boolean renderArms;

        //Bones malone let's gooo
        switch (bone.getName()) {
            case "leftArm", "rightArm" -> {
                bone.setHidden(true);
                bone.setChildrenHidden(false);
                renderArms = true;
            }
            default -> renderArms = false;
        }

        super.renderRecursively(matrixStack, animatable, bone, renderType, bufferSource, this.bufferSource.getBuffer(renderType), isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        if (ClientConfig.instance().laserGunHandRender == ClientConfig.LaserGunHandRender.FAIA) {
            ModelTransformationMode hand;
            boolean leftHanded;
            if (Objects.requireNonNull(client.options.getMainArm().getValue()) == Arm.LEFT) {
                hand = ModelTransformationMode.FIRST_PERSON_LEFT_HAND;
                leftHanded = true;
            } else {
                hand = ModelTransformationMode.FIRST_PERSON_RIGHT_HAND;
                leftHanded = false;
            }

            if (renderArms && this.transformType == hand) {
                PlayerEntityRenderer playerEntityRenderer = (PlayerEntityRenderer) client.getEntityRenderDispatcher().getRenderer(client.player);
                PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = playerEntityRenderer.getModel();

                matrixStack.push();

                RenderUtils.translateMatrixToBone(matrixStack, bone);
                RenderUtils.translateToPivotPoint(matrixStack, bone);
                RenderUtils.rotateMatrixAroundBone(matrixStack, bone);
                RenderUtils.scaleMatrixForBone(matrixStack, bone);
                RenderUtils.translateAwayFromPivotPoint(matrixStack, bone);

                if (client.player == null) return;

                Identifier playerSkin = client.player.getSkinTexture();
                VertexConsumer arm = this.bufferSource.getBuffer(RenderLayer.getEntitySolid(playerSkin));
                VertexConsumer sleeve = this.bufferSource.getBuffer(RenderLayer.getEntityTranslucent(playerSkin));

                float scale = 1 / 2f;
                matrixStack.multiplyPositionMatrix(bone.getModelRotationMatrix());
                if (bone.getName().equals("leftArm")) {
                    matrixStack.scale(scale, scale, scale);
                    if (leftHanded) {
                        matrixStack.translate(-0.23f, -0.03, 0.765);
                    } else {
                        matrixStack.translate(0.015, -0.03, 0.765);
                    }

                    matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));

                    playerEntityModel.leftArm.setPivot(0, 0, 0);
                    playerEntityModel.leftArm.setAngles(0, 0, 0);
                    playerEntityModel.leftArm.render(matrixStack, arm, packedLight, packedOverlay, 1, 1, 1, 1);

                    playerEntityModel.leftSleeve.setPivot(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
                    playerEntityModel.leftSleeve.setAngles(bone.getRotX(), bone.getRotY(), bone.getRotZ());
                    playerEntityModel.leftSleeve.render(matrixStack, sleeve, packedLight, packedOverlay, 1, 1, 1, 1);
                } else if (bone.getName().equals("rightArm")) {
                    matrixStack.scale(scale, scale, scale);
                    if (leftHanded) {
                        matrixStack.translate(0.41f, -0.03, 0.56);

                        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-94.3f));
                        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-7f));
                        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(31f));
                    } else {
                        matrixStack.translate(-0.41f, -0.03, 0.56);

                        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-94.3f));
                        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(7f));
                        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-31f));
                    }

                    playerEntityModel.rightArm.setPivot(0, 0, 0);
                    playerEntityModel.rightArm.setAngles(0, 0, 0);
                    playerEntityModel.rightArm.render(matrixStack, arm, packedLight, packedOverlay, 1, 1, 1, 1);

                    playerEntityModel.rightSleeve.setPivot(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
                    playerEntityModel.rightSleeve.setAngles(bone.getRotX(), bone.getRotY(), bone.getRotZ());
                    playerEntityModel.rightSleeve.render(matrixStack, sleeve, packedLight, packedOverlay, 1, 1, 1, 1);
                }
                matrixStack.pop();
            }
        }
    }

    @Override
    public void updateAnimatedTextureFrame(LaserGun animatable) {
        if (ClientConfig.instance().reducedMotion.isEnabled("laser_power_strip")) {
            animFrameTick = ((int) RenderUtils.getCurrentTick()) - PlayroomClient.ANIMATION_START_TICK.getOrDefault(GeoItem.getId(getCurrentItemStack()), 0);
            AnimatableTexture.setAndUpdate(model.getLayerTextureResource(animatable, getLayerNameByState()), animFrameTick);
        }
        super.updateAnimatedTextureFrame(animatable);
    }

    class EnergyLayer extends GlowingGeoLayer<LaserGun> {

        public EnergyLayer(GeoRenderer<LaserGun> renderer) {
            super(renderer);
        }

        @Override
        protected Identifier getTextureResource(LaserGun animatable) {
            HudRenderer.chargeLayerInfo = getLayerNameByState();
            return model.getLayerTextureResource(animatable, getLayerNameByState());
        }

        @Override
        public void render(MatrixStack poseStack, LaserGun item, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            layerAlphaMultiplier = alphaMulti;

            if (chargeLevel == 100
              || (!item.isCooldownExpired(currentItemStack)
              && item.getCooldownReason(currentItemStack) == LaserGun.CooldownReason.RELOAD)) return;

            if (chargeLevel > 0) {
                layerAlpha = 1 - (chargeLevel / 100f);
            } else {
                layerAlpha = 1;
            }

            render(poseStack, animatable, bakedModel, bufferSource, partialTick);

        }

    }

    private class ChargeLayer extends GlowingGeoLayer<LaserGun> {
        private final int length = 7;

        private ChargeLayer(GeoRenderer<LaserGun> renderer) {
            super(renderer);
        }

        @Override
        protected Identifier getTextureResource(LaserGun animatable) {
            return model.getLayerTextureResource(animatable, LaserGunModel.MAXSTRIPS);
        }

        @Override
        public void render(MatrixStack poseStack, LaserGun item, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            if (!ClientConfig.instance().reducedMotion.isEnabled("laser_charge")) {
                layerAlpha = 0;
                return;
            }

            layerAlphaMultiplier = alphaMulti;
            int cooldown = item.getCooldownLeft(currentItemStack);
            int sinTime = length;

            HudRenderer.animFrameTick = animFrameTick;
            if (cooldown == 1 && item.getCooldownReason(currentItemStack) == LaserGun.CooldownReason.RELOAD) {
                PlayroomClient.ANIMATION_START_TICK.put(GeoItem.getId(currentItemStack), (int) RenderUtils.getCurrentTick());
            }

            if (cooldown > 0) {
                sinTime = Math.max(item.getCooldownTime(currentItemStack), length);
            }

            if (chargeLevel > 0) {
                layerAlpha = chargeLevel / 100f;
            } else if (animFrameTick < sinTime) {
                // This must start at full alpha since it is used to hide the animation resetting, if we fade it in the fame reset will be visible
                layerAlpha = MathHelper.clamp(1 - (animFrameTick / (float) length), 0, 1);
            } else if (cooldown > 0) {
//				float midPoint = reloadTime / 2f * .7f;
                layerAlpha = 1 - (cooldown / (float) item.getCooldownTime(currentItemStack));
            } else {
                return;
            }

            HudRenderer.chargeLayerAlpha = layerAlpha;

            render(poseStack, animatable, bakedModel, bufferSource, partialTick);

        }

    }

}
