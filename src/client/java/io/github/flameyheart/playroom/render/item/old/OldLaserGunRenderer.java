package io.github.flameyheart.playroom.render.item.old;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.compat.ModOptional;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.item.OldLaserGun;
import io.github.flameyheart.playroom.mixin.client.geo.AutoGlowingTextureAccessor;
import io.github.flameyheart.playroom.mixin.geo.AnimationControllerAccessor;
import io.github.flameyheart.playroom.render.hud.HudRenderer;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.texture.AnimatableTexture;
import software.bernie.geckolib.cache.texture.GeoAbstractTexture;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtils;

import java.util.Objects;
import java.util.function.Function;

// TODO: Remove
public class OldLaserGunRenderer extends AlternativeGeoItemRenderer<OldLaserGun> {
    private static final boolean IS_IRIS_PRESENT = ModOptional.isPresent("iris");

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final float alphaMulti = (IS_IRIS_PRESENT && IrisApi.getInstance().isShaderPackInUse()) ? .25f : 1f;

    private VertexConsumerProvider bufferSource;
    private ModelTransformationMode transformType;
    private int chargeLevel;

    public OldLaserGunRenderer() {
        super(new OldLaserGunModel());
//        addRenderLayer(new EnergyLayer(this));
//        addRenderLayer(new ChargeLayer(this));
        
        addRenderLayer(new OldStripLayer(this));
        addRenderLayer(new OldEnergyFlowLayer(this));
        addRenderLayer(new OldChargeLayer(this));
        addRenderLayer(new OldStripGlowLayer(this));
        addRenderLayer(new OldDisabledLayer(this));
    }

    @Override
    protected boolean useAlternativeTexture(OldLaserGun animatable) {
        return animatable.isRapidFire(getCurrentItemStack());
    }

    @Override
    public Identifier getTextureLocation(OldLaserGun animatable) {
        return getGeoModel().getTextureResource(animatable);
    }

    /** 
     * Resets glow anim after change mode
     */
    @Override
    public void updateAnimatedTextureFrame(OldLaserGun animatable) {
        int offset = PlayroomClient.ANIMATION_START_TICK.getOrDefault(getInstanceId(animatable), 0);
//        HudRenderer.animOffset = animatable.getTick(animatable) - offset;
        AnimatableTexture.setAndUpdate(getTextureLocation(animatable), (int) animatable.getTick(animatable) - offset);
        AnimatableTexture.setAndUpdate(GeoAbstractTexture.appendToPath(super.getTextureLocation(animatable), "_glowmask"), (int) animatable.getTick(animatable) - offset);
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode transformType, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
        this.bufferSource = bufferSource;
        //this.renderType = type;
        this.transformType = transformType;
        super.render(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);

        if (stack.getItem() instanceof OldLaserGun item && item.isRapidFire(stack)) {
            AnimationController<GeoAnimatable> controller = item.getAnimationController(stack);
            if (controller != null) {
                long geoId = GeoItem.getId(stack);
                if (((AnimationControllerAccessor) controller).getTriggeredAnimation() == null) {
                    item.triggerAnim(MinecraftClient.getInstance().player, geoId, "controller", "rapidfire_mode");
                }
            }
        }
    }

    @Override
    public void renderRecursively(MatrixStack matrixStack, OldLaserGun animatable, GeoBone bone, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.renderRecursively(matrixStack, animatable, bone, renderType, bufferSource, this.bufferSource.getBuffer(renderType), isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        
        if(isReRender)
            return;
        
        chargeLevel = animatable.getPlayroomTag(getCurrentItemStack()).getInt("Charge");
        
        if(bone.getName().lastIndexOf("Arm") != -1) {
            bone.setHidden(true);
            bone.setChildrenHidden(false);
        }
        else
            return;
        
        ModelTransformationMode hand;
        boolean leftHanded;
        if (Objects.requireNonNull(client.options.getMainArm().getValue()) == Arm.LEFT) {
            hand = ModelTransformationMode.FIRST_PERSON_LEFT_HAND;
            leftHanded = true;
        } else {
            hand = ModelTransformationMode.FIRST_PERSON_RIGHT_HAND;
            leftHanded = false;
        }

        if (this.transformType == hand) {
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
                if(leftHanded) {
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
                if(leftHanded) {
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
    
    /**
     * 
     */
    private class EnergyLayer extends AutoGlowingGeoLayer<OldLaserGun> {
    
        private EnergyLayer(GeoRenderer<OldLaserGun> renderer) {
            super(renderer);
        }
    
        @Override
        protected RenderLayer getRenderType(OldLaserGun animatable) {
            return AnimatedAutoGlowingTexture.getRenderType(Playroom.id("textures/item/old/laser_gun_strips.png"));
        }
    
        @Override
        public void render(MatrixStack poseStack, OldLaserGun item, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            
            if(chargeLevel == 100
            || !item.isCooldownExpired(currentItemStack)
            && item.getCooldownReason(currentItemStack) == OldLaserGun.CooldownReason.RELOAD)
                return;
            
            RenderLayer emissiveRenderType = getRenderType(item);
            getRenderer().reRender(bakedModel, poseStack, bufferSource, item, emissiveRenderType,
                    bufferSource.getBuffer(emissiveRenderType), partialTick, 0xF0_00_00, OverlayTexture.DEFAULT_UV,
                    1, 1, 1, .75f * alphaMulti);
        
        }
    
    }
    
    private class ChargeLayer extends AutoGlowingGeoLayer<OldLaserGun> {
    
        private float alpha;
    
        private ChargeLayer(GeoRenderer<OldLaserGun> renderer) {
            super(renderer);
        }
    
        @Override
        protected RenderLayer getRenderType(OldLaserGun animatable) {
            return AnimatedAutoGlowingTexture.getRenderType(Playroom.id("textures/item/old/laser_gun_strips_glow.png"));
        }
    
        @Override
        public void render(MatrixStack poseStack, OldLaserGun item, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            
            int cooldown = item.getCooldownLeft(currentItemStack);
            double transitionAnimTick = PlayroomClient.ANIMATION_START_TICK.getOrDefault(getInstanceId(animatable), 0);
            
            if(chargeLevel > 0)
                alpha = chargeLevel / 100f;
            else if(cooldown > 0) {
                
                int midPoint = ServerConfig.instance().laserFireReloadTime / 2;
                
                if(cooldown < midPoint);
                
//                PlayroomClient.LOGGER.info("cooldown: {}", cooldown);
//                PlayroomClient.LOGGER.info("cooldown / (midPoint * .7f): {}", cooldown / (midPoint * .7f));
                
//                alpha = MathHelper.clamp(cooldown / (midPoint * .7f), 0, .7f);
                
                if(cooldown == 1)
                    PlayroomClient.ANIMATION_START_TICK.put(getInstanceId(item), (int) RenderUtils.getCurrentTick());
            
            }
            else
                return;
            
            RenderLayer emissiveRenderType = getRenderType(item);
            getRenderer().reRender(bakedModel, poseStack, bufferSource, item, emissiveRenderType,
                    bufferSource.getBuffer(emissiveRenderType), partialTick, 0xF0_00_00, OverlayTexture.DEFAULT_UV,
                    1, 1, 1, alpha * alphaMulti);
        
        }
    
    }

    /* 
     * ======================================
     * 
     *               OLD CODE
     * 
     * ======================================
     */

    /**
     * Handles default charged state
     */
    private class OldStripLayer extends AutoGlowingGeoLayer<OldLaserGun> {
        private OldStripLayer(GeoRenderer<OldLaserGun> renderer) {
            super(renderer);
        }

        @Override
        protected RenderLayer getRenderType(OldLaserGun animatable) {
            return AnimatedAutoGlowingTexture.getRenderType(Playroom.id("textures/item/old/laser_gun_strips.png"));
        }

        @Override
        public void render(MatrixStack poseStack, OldLaserGun animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            if (!animatable.isCooldownExpired(getCurrentItemStack()) && animatable.getCooldownReason(getCurrentItemStack()) == OldLaserGun.CooldownReason.RELOAD) return;
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
    
    /**
     * Handles default charged state animation
     */
    private class OldEnergyFlowLayer extends AutoGlowingGeoLayer<OldLaserGun> {
        public OldEnergyFlowLayer(GeoRenderer<OldLaserGun> renderer) {
            super(renderer);
        }

        @Override
        protected RenderLayer getRenderType(OldLaserGun animatable) {
            return AnimatedAutoGlowingTexture.getRenderType(OldLaserGunRenderer.super.getTextureLocation(animatable));
        }

        @Override
        public void render(MatrixStack poseStack, OldLaserGun animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            if (!animatable.isCooldownExpired(getCurrentItemStack()) && animatable.getCooldownReason(getCurrentItemStack()) == OldLaserGun.CooldownReason.RELOAD) return;
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

    /**
     * Handles holding trigger
     */
    private class OldChargeLayer extends AutoGlowingGeoLayer<OldLaserGun> {
        public OldChargeLayer(GeoRenderer<OldLaserGun> renderer) {
            super(renderer);
        }

        @Override
        protected RenderLayer getRenderType(OldLaserGun animatable) {
            return AnimatedAutoGlowingTexture.getRenderType(Playroom.id("textures/item/old/laser_gun_strips_glow.png"));
        }

        @Override
        public void render(MatrixStack poseStack, OldLaserGun item, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            if (!animatable.isCooldownExpired(getCurrentItemStack()) && animatable.getCooldownReason(getCurrentItemStack()) == OldLaserGun.CooldownReason.RELOAD) return;
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

    /**
     * Handles transition between modes
     */
    private class OldStripGlowLayer extends AutoGlowingGeoLayer<OldLaserGun> {
        private OldStripGlowLayer(GeoRenderer<OldLaserGun> renderer) {
            super(renderer);
        }

        @Override
        protected RenderLayer getRenderType(OldLaserGun animatable) {
            return AnimatedAutoGlowingTexture.getRenderType(Playroom.id("textures/item/old/laser_gun_strips_glow.png"));
        }

        @Override
        public void render(MatrixStack poseStack, OldLaserGun animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            RenderLayer emissiveRenderType = getRenderType(animatable);
            float alphaMax;
            if (IS_IRIS_PRESENT && IrisApi.getInstance().isShaderPackInUse()) {
                alphaMax = 0.25f;
            } else {
                alphaMax = 1f;
            }

            float alpha;
            long length = 8;
            double animationStart = PlayroomClient.ANIMATION_START_TICK.getOrDefault(getInstanceId(animatable), 0);
            
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
            HudRenderer.chargeLayerAlpha = alpha;


            getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveRenderType,
              bufferSource.getBuffer(emissiveRenderType), partialTick, 0xF0_00_00, OverlayTexture.DEFAULT_UV,
              1, 1, 1, alpha * alphaMax);
        }
    }

    /**
     * Handles the cooldown animation
     */
    private class OldDisabledLayer extends GeoRenderLayer<OldLaserGun> {
        private static final Identifier TEXTURE = Playroom.id("textures/item/old/laser_gun_disabled.png");
        private OldDisabledLayer(GeoRenderer<OldLaserGun> renderer) {
            super(renderer);
        }

        private static final Function<Identifier, RenderLayer> RENDER_TYPE_FUNCTION = Util.memoize(texture -> {
            RenderPhase.Texture textureState = new RenderPhase.Texture(texture, false, false);

            return RenderLayer.of("geo_glowing_layer",
              VertexFormats.POSITION_COLOR_TEXTURE, VertexFormat.DrawMode.QUADS,
              256, false, true,
              RenderLayer.MultiPhaseParameters.builder()
                .program(RenderPhase.ShaderProgram.POSITION_COLOR_TEXTURE_PROGRAM)
                .texture(textureState)
                .transparency(AutoGlowingTextureAccessor.getTRANSPARENCY_STATE())
                .writeMaskState(AutoGlowingTextureAccessor.getWRITE_MASK())
                .build(false));
        });

        @Override
        public void render(MatrixStack poseStack, OldLaserGun animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            if (animatable.isCooldownExpired(getCurrentItemStack()) || animatable.getCooldownReason(getCurrentItemStack()) != OldLaserGun.CooldownReason.RELOAD) return;
            RenderLayer renderLayer = RENDER_TYPE_FUNCTION.apply(TEXTURE);

            float alpha;

            ItemStack stack = getCurrentItemStack();
            int cooldown = animatable.getCooldownLeft(stack);

            //make alphaMultiplier fade in and out
            int midPoint = ServerConfig.instance().laserFireReloadTime / 2;
            if (cooldown < 1) {
                PlayroomClient.ANIMATION_START_TICK.put(getInstanceId(animatable), (int) RenderUtils.getCurrentTick());
                alpha = 0.7f;
            } else if (cooldown < midPoint * 0.7f) {
                alpha = MathHelper.clamp(cooldown / (midPoint * 0.7f), 0, 0.7f);
            } else {
                alpha = 1;
            }
            
            getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, renderLayer,
              bufferSource.getBuffer(renderLayer), partialTick, 0xF0_00_00, OverlayTexture.DEFAULT_UV,
              1, 1, 1, HudRenderer.chargeLayerAlpha = alpha);
        }
    }
}
