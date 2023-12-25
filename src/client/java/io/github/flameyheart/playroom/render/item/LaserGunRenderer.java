package io.github.flameyheart.playroom.render.item;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.compat.ModOptional;
import io.github.flameyheart.playroom.config.ClientConfig;
import io.github.flameyheart.playroom.item.LaserGun;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.texture.AnimatableTexture;
import software.bernie.geckolib.cache.texture.GeoAbstractTexture;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.util.RenderUtils;

public class LaserGunRenderer extends AlternativeGeoItemRenderer<LaserGun> {
    private static final boolean IS_IRIS_PRESENT = ModOptional.isPresent("iris");

    private VertexConsumerProvider bufferSource;
    private ModelTransformationMode transformType;

    public LaserGunRenderer() {
        super(new LaserGunModel());
        addRenderLayer(new StripLayer(this));
        addRenderLayer(new EnergyFlowLayer(this));
        addRenderLayer(new ChargeLayer(this));
        addRenderLayer(new StripGlowLayer(this));
    }

    @Override
    protected boolean useAlternativeTexture(LaserGun animatable) {
        return animatable.getPlayroomTag(getCurrentItemStack()).getBoolean("RapidFire");
    }

    @Override
    public void updateAnimatedTextureFrame(LaserGun animatable) {
        int offset = (int) PlayroomClient.ANIMATION_START_TICK.getOrDefault(getInstanceId(animatable), 0d).doubleValue();
        AnimatableTexture.setAndUpdate(getTextureLocation(animatable), (int) animatable.getTick(animatable) - offset);
        AnimatableTexture.setAndUpdate(GeoAbstractTexture.appendToPath(getTextureLocation(animatable), "_glowmask"), (int) animatable.getTick(animatable) - offset);
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode transformType, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
        this.bufferSource = bufferSource;
        //this.renderType = type;
        this.transformType = transformType;
        super.render(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
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

        if (renderArms && this.transformType == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND) {
            PlayerEntityRenderer playerEntityRenderer = (PlayerEntityRenderer) client.getEntityRenderDispatcher().getRenderer(client.player);
            PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = playerEntityRenderer.getModel();

            matrixStack.push();

            RenderUtils.translateMatrixToBone(matrixStack, bone);
            RenderUtils.translateToPivotPoint(matrixStack, bone);
            RenderUtils.rotateMatrixAroundBone(matrixStack, bone);
            RenderUtils.scaleMatrixForBone(matrixStack, bone);
            RenderUtils.translateAwayFromPivotPoint(matrixStack, bone);

            assert (client.player != null);

            Identifier playerSkin = client.player.getSkinTexture();
            VertexConsumer arm = this.bufferSource.getBuffer(RenderLayer.getEntitySolid(playerSkin));
            VertexConsumer sleeve = this.bufferSource.getBuffer(RenderLayer.getEntityTranslucent(playerSkin));

            ClientConfig c = ClientConfig.instance();
            float scale = 1 / 2f;
            matrixStack.multiplyPositionMatrix(bone.getModelRotationMatrix());
            if (bone.getName().equals("rightArm")) {
                matrixStack.scale(scale, scale, scale);
                matrixStack.translate(0.015, -0.03, 0.765);
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
                playerEntityModel.leftArm.setPivot(0, 0, 0);
                playerEntityModel.leftArm.setAngles(0, 0, 0);
                playerEntityModel.leftArm.render(matrixStack, arm, packedLight, packedOverlay, 1, 1, 1, 1);

                playerEntityModel.leftSleeve.setPivot(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
                playerEntityModel.leftSleeve.setAngles(bone.getRotX(), bone.getRotY(), bone.getRotZ());
                playerEntityModel.leftSleeve.render(matrixStack, sleeve, packedLight, packedOverlay, 1, 1, 1, 1);
            } else if (bone.getName().equals("leftArm")) {
                matrixStack.scale(scale, scale, scale);
                matrixStack.translate(-0.41f, -0.03, 0.56);

                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-94.320114f));
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(7f));
                matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-31));

                //playerEntityModel.rightArm.setPivot(-8, -0.4f, 8.45f);
                playerEntityModel.rightArm.setPivot(0, 0, 0);
                //playerEntityModel.rightArm.setAngles(-1.72f, -0.7330383f, 0.04f);
                playerEntityModel.rightArm.setAngles(0, 0, 0);
                playerEntityModel.rightArm.render(matrixStack, arm, packedLight, packedOverlay, 1, 1, 1, 1);

                playerEntityModel.rightSleeve.setPivot(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
                playerEntityModel.rightSleeve.setAngles(bone.getRotX(), bone.getRotY(), bone.getRotZ());
                playerEntityModel.rightSleeve.render(matrixStack, sleeve, packedLight, packedOverlay, 1, 1, 1, 1);
            }
            matrixStack.pop();
        }
    }

    private static class StripLayer extends AutoGlowingGeoLayer<LaserGun> {
        private StripLayer(GeoRenderer<LaserGun> renderer) {
            super(renderer);
        }

        @Override
        protected RenderLayer getRenderType(LaserGun animatable) {
            return AnimatedAutoGlowingTexture.getRenderType(Playroom.id("textures/item/laser_gun_strips.png"));
        }

        @Override
        public void render(MatrixStack poseStack, LaserGun animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            RenderLayer emissiveRenderType = getRenderType(animatable);
            float alpha = 0.5f;
            if (IS_IRIS_PRESENT && IrisApi.getInstance().isShaderPackInUse()) {
                alpha = 0.10f;
            }

            getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveRenderType,
              bufferSource.getBuffer(emissiveRenderType), partialTick, 0xF0_00_00, OverlayTexture.DEFAULT_UV,
              1, 1, 1, alpha);
        }
    }

    private class EnergyFlowLayer extends AutoGlowingGeoLayer<LaserGun> {
        public EnergyFlowLayer(GeoRenderer<LaserGun> renderer) {
            super(renderer);
        }

        @Override
        protected RenderLayer getRenderType(LaserGun animatable) {
            return AnimatedAutoGlowingTexture.getRenderType(getTextureLocation(animatable));
        }

        @Override
        public void render(MatrixStack poseStack, LaserGun animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            RenderLayer emissiveRenderType = getRenderType(animatable);
            float alpha = 1f;
            if (IS_IRIS_PRESENT && IrisApi.getInstance().isShaderPackInUse()) {
                alpha = 0.25f;
            }

            getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveRenderType,
              bufferSource.getBuffer(emissiveRenderType), partialTick, 0xF0_00_00, OverlayTexture.DEFAULT_UV,
              1, 1, 1, alpha);
        }
    }

    private class ChargeLayer extends AutoGlowingGeoLayer<LaserGun> {
        public ChargeLayer(GeoRenderer<LaserGun> renderer) {
            super(renderer);
        }

        @Override
        protected RenderLayer getRenderType(LaserGun animatable) {
            return AnimatedAutoGlowingTexture.getRenderType(Playroom.id("textures/item/laser_gun_strips_glow.png"));
        }

        @Override
        public void render(MatrixStack poseStack, LaserGun item, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            ItemStack stack = getCurrentItemStack();
            if (item.isRapidFire(stack)) return;
            RenderLayer emissiveRenderType = getRenderType(item);
            float alphaMultiplier = 1f;
            if (IS_IRIS_PRESENT && IrisApi.getInstance().isShaderPackInUse()) {
                alphaMultiplier = 0.25f;
            }

            float alpha;
            int charge = item.getPlayroomTag(stack).getInt("Charge");
            if (charge > 0) {
                alpha = charge / 100f;
            } else {
                alpha = 0;
            }

            getRenderer().reRender(bakedModel, poseStack, bufferSource, item, emissiveRenderType,
              bufferSource.getBuffer(emissiveRenderType), partialTick, 0xF0_00_00, OverlayTexture.DEFAULT_UV,
              1, 1, 1, alpha * alphaMultiplier);
        }
    }

    private class StripGlowLayer extends AutoGlowingGeoLayer<LaserGun> {
        private StripGlowLayer(GeoRenderer<LaserGun> renderer) {
            super(renderer);
        }

        @Override
        protected RenderLayer getRenderType(LaserGun animatable) {
            return AnimatedAutoGlowingTexture.getRenderType(Playroom.id("textures/item/laser_gun_strips_glow.png"));
        }

        @Override
        public void render(MatrixStack poseStack, LaserGun animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            RenderLayer emissiveRenderType = getRenderType(animatable);
            float alphaMax;
            if (IS_IRIS_PRESENT && IrisApi.getInstance().isShaderPackInUse()) {
                alphaMax = 0.25f;
            } else {
                alphaMax = 1f;
            }

            float alpha;
            long length = 8;
            double animationStart = PlayroomClient.ANIMATION_START_TICK.getOrDefault(getInstanceId(animatable), 0d);

            double fadeinStart = animationStart + length;
            double currentTick = RenderUtils.getCurrentTick();
            double fadeoutEnd = animationStart + length * 2;
            //make alphaMultiplier fade in and out
            if (currentTick < animationStart) {
                //get progress between fadeout start and fadeout end
                double startTime = currentTick - fadeinStart;
                float progress = (float) startTime / (float) length;
                alpha = MathHelper.clamp(progress, 0, 1);
            } else if (currentTick < fadeoutEnd) {
                //get progress between fadeout start and fadeout end
                double startTime = currentTick - animationStart;
                float progress = (float) startTime / (float) length;
                alpha = MathHelper.clamp(1 - progress, 0, 1);
            } else {
                alpha = 0;
            }


            getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveRenderType,
              bufferSource.getBuffer(emissiveRenderType), partialTick, 0xF0_00_00, OverlayTexture.DEFAULT_UV,
              1, 1, 1, alpha * alphaMax);
        }
    }
}
