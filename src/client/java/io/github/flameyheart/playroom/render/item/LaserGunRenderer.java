package io.github.flameyheart.playroom.render.item;

import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.config.ClientConfig;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.item.LaserGun;
import io.github.flameyheart.playroom.mixin.compat.geo.AnimationControllerAccessor;
import io.github.flameyheart.playroom.render.hud.HudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
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

	private final LaserGunModel model;
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final ServerConfig serverConfig = ServerConfig.instance();

	private int animFrameTick;
	private int chargeLevel;
	private float alphaMulti;

	public LaserGunRenderer() {
		
		super(new LaserGunModel());
		
		model = (LaserGunModel) getGeoModel();
		
		addRenderLayer(new EnergyLayer(this));
		addRenderLayer(new ChargeLayer(this));
	
	}

	public String getLayerNameByState() {
		return animatable == null ? LaserGunModel.ENABLED_RANGEMODE : animatable.isRapidFire(getCurrentItemStack()) ? LaserGunModel.ENABLED_RAPIDFIREMODE : LaserGunModel.ENABLED_RANGEMODE;
	}

	@Override
	public void render(ItemStack stack, ModelTransformationMode transformType, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
		
		boolean isFirstPerson = transformType.isFirstPerson();
		
		currentItemStack = stack;
		animatable = (LaserGun) stack.getItem();
		alphaMulti = isFirstPerson ? (PlayroomClient.isIrisInUse ? .3f : 1f) : 1;
		
		if(animatable.isRapidFire(stack)) {
			AnimationController<GeoAnimatable> controller = animatable.getAnimationController(stack);
			if(controller != null && ((AnimationControllerAccessor) controller).getTriggeredAnimation() == null) {
				controller.tryTriggerAnimation("rapidfire_mode");
			}
		}
		
		chargeLevel = animatable.getPlayroomTag(stack).getInt("Charge");
		
		animFrameTick = ClientConfig.instance().reducedMotion.isEnabled("laser_power_strip") ? ((int) RenderUtils.getCurrentTick()) - PlayroomClient.ANIMATION_START_TICK.getOrDefault(GeoItem.getId(getCurrentItemStack()), 0) : 0;
		AnimatableTexture.setAndUpdate(model.getLayerTextureResource(animatable, getLayerNameByState()), animFrameTick);
		
		super.render(stack, transformType, poseStack, bufferSource, transformType == ModelTransformationMode.GUI ? LightmapTextureManager.MAX_LIGHT_COORDINATE : packedLight, 0);
		
		if (!isFirstPerson) return;
		
		AbstractClientPlayerEntity player = client.player;
		Identifier playerSkin = player.getSkinTexture();
		VertexConsumer armTexture = bufferSource.getBuffer(RenderLayer.getEntitySolid(playerSkin));
		VertexConsumer sleeveTexture = bufferSource.getBuffer(RenderLayer.getEntityTranslucent(playerSkin));
		
		PlayerEntityRenderer playerEntityRenderer = (PlayerEntityRenderer) client.getEntityRenderDispatcher().getRenderer(player);
		PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = playerEntityRenderer.getModel();
		ModelPart playerLeftArm = playerEntityModel.leftArm;
		ModelPart playerLeftSleeve = playerEntityModel.leftSleeve;
		ModelPart playerRightArm = playerEntityModel.rightArm;
		ModelPart playerRightSleeve = playerEntityModel.rightSleeve;
		
		// TODO: Combine both Awakened and My Model Styles
		boolean rightArm = player.getMainArm() == Arm.RIGHT;
		boolean modelAccurate = ClientConfig.instance().laserGunHandRender == ClientConfig.LaserGunHandRender.MAC;
		float scale = modelAccurate ? .6666f : .5f;
		
		poseStack.push();
		poseStack.scale(scale, scale, scale);
		
		if(player.getModel().equals("default")) {
			if(rightArm) {
				playerRightArm.setAngles(1.5707f, 3.142f, 0);
				
				if(modelAccurate) {
					playerLeftArm.setPivot(2.2512f, 11.5f, 24.251f);
					playerLeftArm.setAngles(1.6707f, 2.356f, 0);
					playerRightArm.setPivot(12.2512f, 11.5f, 24.251f);
				}
				else {
					playerLeftArm.setPivot(8.7f, 16.5f, 26.0f);
					playerLeftArm.setAngles(1.71f, 2.52f, -.1f);
					playerRightArm.setPivot(16.805f, 13.84f, 28.2351f);
				}
			}
			else {
				playerLeftArm.setAngles(1.5707f, 3.142f, 0);
				
				if(modelAccurate) {
					playerLeftArm.setPivot(10.2512f, 11.5f, 24.251f);
					playerRightArm.setPivot(20.1952f, 11.5f, 23.261f);
					playerRightArm.setAngles(1.5707f, 3.741f, 0);
				}
				else {
					playerLeftArm.setPivot(13.805f, 13.84f, 28.2351f);
					playerRightArm.setPivot(21.6952f, 16.34f, 26.761f);
					playerRightArm.setAngles(1.6707f, 3.741f, .1f);
				}
			}
		}
		else {
			if(rightArm) {
				playerRightArm.setAngles(1.5707f, 3.142f, 0);
				
				if(modelAccurate) {
					playerLeftArm.setPivot(2.7512f, 11.5f, 24.251f);
					playerLeftArm.setAngles(1.6707f, 2.356f, 0);
					playerRightArm.setPivot(16.305f, 15.84f, 28.2351f);
				}
				else {
					playerLeftArm.setPivot(8.7f, 16.5f, 26.0f);
					playerLeftArm.setAngles(1.71f, 2.52f, -.1f);
					playerRightArm.setPivot(20.8588f, 13.84f, 28.2351f);
				}
			}
			else {
				playerLeftArm.setAngles(1.5707f, 3.142f, 0);
				
				if(modelAccurate) {
					playerLeftArm.setPivot(9.7512f, 11.5f, 24.301f);
					playerRightArm.setPivot(19.6847f, 11.5f, 23.338f);
					playerRightArm.setAngles(1.5707f, 3.739f, 0);
				}
				else {
					playerLeftArm.setPivot(13.305f, 13.84f, 28.2351f);
					playerRightArm.setPivot(21.6952f, 16.34f, 26.761f);
					playerRightArm.setAngles(1.6707f, 3.741f, .1f);
				}
			}
		}
		
		playerLeftSleeve.copyTransform(playerLeftArm);
		playerRightSleeve.copyTransform(playerRightArm);
		
		int playerOverlay = LivingEntityRenderer.getOverlay(player, 0);
		
		playerLeftArm.render(poseStack, armTexture, packedLight, playerOverlay);
		playerLeftSleeve.render(poseStack, sleeveTexture, packedLight, playerOverlay);
		playerRightArm.render(poseStack, armTexture, packedLight, playerOverlay);
		playerRightSleeve.render(poseStack, sleeveTexture, packedLight, playerOverlay);
		
		poseStack.pop();
	
	}

	class EnergyLayer extends GlowingGeoLayer<LaserGun> {
	
		public EnergyLayer(GeoRenderer<LaserGun> renderer) {
			super(renderer);
		}
	
		@Override
		protected Identifier getTextureResource(LaserGun animatable) {
			return model.getLayerTextureResource(animatable, getLayerNameByState());
		}
	
		@Override
		public void render(MatrixStack poseStack, LaserGun item, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
			
			layerAlphaMultiplier = alphaMulti;
			
			if((!item.isCooldownExpired(currentItemStack)
					&& item.getCooldownReason(currentItemStack) == LaserGun.CooldownReason.RELOAD))
				return;
			
			if(ClientConfig.instance().reducedMotion.isEnabled("laser_charge")) {
				if(chargeLevel == 100)
					return;
				
				if(chargeLevel > 0) {
					layerAlpha = 1 - (chargeLevel / 100f);
				}
				else {
					layerAlpha = 1;
				}
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
			
			if (!ClientConfig.instance().reducedMotion.isEnabled("laser_charge")) return;
			
			layerAlphaMultiplier = alphaMulti;
			int cooldown = item.getCooldownLeft(currentItemStack);
			
			if (cooldown == 1 && item.getCooldownReason(currentItemStack) == LaserGun.CooldownReason.RELOAD) {
				PlayroomClient.ANIMATION_START_TICK.put(GeoItem.getId(currentItemStack), (int) RenderUtils.getCurrentTick());
			}
			
			if(chargeLevel > 0)
				layerAlpha = chargeLevel / 100f;
			else if(animFrameTick < length)
				layerAlpha = MathHelper.clamp(1 - ((float) animFrameTick / length), 0, 1);
			else if(cooldown > 0)
				layerAlpha = Math.max(.55f - ((float) cooldown / serverConfig.laserFireReloadTime), 0);
			else
				return;
			
			render(poseStack, animatable, bakedModel, bufferSource, partialTick);
		
		}
	
	}

}
