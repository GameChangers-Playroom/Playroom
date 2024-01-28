package io.github.flameyheart.playroom.render.item;

import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.compat.ModOptional;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.item.LaserGun;
import io.github.flameyheart.playroom.mixin.client.geo.AutoGlowingTextureAccessor;
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
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.AnimatableTexture;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.util.RenderUtils;

public class LaserGunRenderer extends GeoItemRenderer<LaserGun> {

	private static final boolean IS_IRIS_PRESENT = ModOptional.isPresent("iris");

	private final LaserGunModel model;
	private final MinecraftClient client = MinecraftClient.getInstance();

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
		return animatable == null ? LaserGunModel.ENABLED_RANGEMODE : animatable.isRapidFire(currentItemStack) ? LaserGunModel.ENABLED_RAPIDFIREMODE : LaserGunModel.ENABLED_RANGEMODE;
	}

	/** TODO: Add animation callback */
	@Override
	public void render(ItemStack stack, ModelTransformationMode transformType, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {

		boolean isFirstPerson = transformType.isFirstPerson();

		currentItemStack = stack;
		animatable = (LaserGun) stack.getItem();
		alphaMulti = isFirstPerson ? (IS_IRIS_PRESENT && IrisApi.getInstance().isShaderPackInUse() ? .3f : 1f) : 1;

		if(animatable.isRapidFire(stack)) {

			AnimationController<GeoAnimatable> controller = animatable.getAnimationController(stack);
			if(controller != null)
				controller.tryTriggerAnimation("rapidfire_mode");

		}

		chargeLevel = animatable.getPlayroomTag(stack).getInt("Charge");
		animFrameTick = ((int) RenderUtils.getCurrentTick()) - PlayroomClient.ANIMATION_START_TICK.getOrDefault(GeoItem.getId(stack), 0);
//		AnimatableTexture.setAndUpdate(model.getLayerTextureResource(animatable, getLayerNameByState(animatable)), animFrameTick);

		super.render(stack, transformType, poseStack, bufferSource, transformType == ModelTransformationMode.GUI ? LightmapTextureManager.MAX_LIGHT_COORDINATE : packedLight, 0);

		if(!isFirstPerson) return;

		AbstractClientPlayerEntity player = client.player;
		Identifier playerSkin = player.getSkinTexture();
		VertexConsumer armTexture = bufferSource.getBuffer(RenderLayer.getEntitySolid(playerSkin));
		VertexConsumer sleeveTexture = bufferSource.getBuffer(RenderLayer.getEntityTranslucent(playerSkin));

		PlayerEntityRenderer playerEntityRenderer = (PlayerEntityRenderer) client.getEntityRenderDispatcher().getRenderer(player);
		PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = playerEntityRenderer.getModel();
		float scale = .6666f;

		ModelTransform leftSleeveTransform = playerEntityModel.leftSleeve.getTransform();
		ModelTransform rightSleeveTransform = playerEntityModel.rightSleeve.getTransform();

		poseStack.push();
		poseStack.scale(scale, scale, scale);

		if(player.getModel().equals("default")) {
			if(player.getMainArm() == Arm.RIGHT) {
				playerEntityModel.leftArm.setPivot(2.2512f, 11.5f, 24.251f);
				playerEntityModel.leftArm.setAngles(1.6707f, 2.356f, 0);
				playerEntityModel.rightArm.setPivot(12.2512f, 11.5f, 24.251f);
				playerEntityModel.rightArm.setAngles(1.5707f, 3.142f, 0);
			}
			else if(player.getMainArm() == Arm.LEFT) {
				playerEntityModel.leftArm.setPivot(10.2512f, 11.5f, 24.251f);
				playerEntityModel.leftArm.setAngles(1.5707f, 3.142f, 0);
				playerEntityModel.rightArm.setPivot(20.1952f, 11.5f, 23.261f);
				playerEntityModel.rightArm.setAngles(1.5707f, 3.741f, 0);
			}
		}
		else if(player.getModel().equals("slim")) {
			if(player.getMainArm() == Arm.RIGHT) {
				playerEntityModel.leftArm.setPivot(2.7512f, 11.5f, 24.251f);
				playerEntityModel.leftArm.setAngles(1.6707f, 2.356f, 0);
				playerEntityModel.rightArm.setPivot(12.7512f, 11.5f, 24.251f);
				playerEntityModel.rightArm.setAngles(1.5707f, 3.142f, 0);
			}
			else if(player.getMainArm() == Arm.LEFT) {
				playerEntityModel.leftArm.setPivot(9.7512f, 11.5f, 24.301f);
				playerEntityModel.leftArm.setAngles(1.5707f, 3.142f, 0);
				playerEntityModel.rightArm.setPivot(19.6847f, 11.5f, 23.338f);
				playerEntityModel.rightArm.setAngles(1.5707f, 3.739f, 0);
			}
		}

		playerEntityModel.leftSleeve.copyTransform(playerEntityModel.leftArm);
		playerEntityModel.rightSleeve.copyTransform(playerEntityModel.rightArm);

		int playerOverlay = LivingEntityRenderer.getOverlay(player,0);

		playerEntityModel.leftArm.render(poseStack, armTexture, packedLight, playerOverlay);
		playerEntityModel.leftSleeve.render(poseStack, sleeveTexture, packedLight, playerOverlay);
		playerEntityModel.rightArm.render(poseStack, armTexture, packedLight, playerOverlay);
		playerEntityModel.rightSleeve.render(poseStack, sleeveTexture, packedLight, playerOverlay);

		poseStack.pop();

		playerEntityModel.leftSleeve.setTransform(leftSleeveTransform);
		playerEntityModel.rightSleeve.setTransform(rightSleeveTransform);

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

			if(chargeLevel == 100
			|| (!item.isCooldownExpired(currentItemStack)
			 && item.getCooldownReason(currentItemStack) == LaserGun.CooldownReason.RELOAD)) return;

			if(chargeLevel > 0)
				layerAlpha = 1 - (chargeLevel / 100f);
			else
				layerAlpha = 1;

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

			layerAlphaMultiplier = alphaMulti;
			int cooldown = currentItemStack.getItemBarStep();

			HudRenderer.animFrameTick = animFrameTick;

			if(chargeLevel > 0)
				layerAlpha = chargeLevel / 100f;
			else if(animFrameTick < length)
				layerAlpha = MathHelper.sin(animFrameTick * (MathHelper.PI / length)) * 1.1f;
			else if(cooldown < 14) {

//				float midPoint = reloadTime / 2f * .7f;

				layerAlpha = cooldown / 13f;

			}
			else return;

			HudRenderer.chargeLayerAlpha = chargeLevel;

			render(poseStack, animatable, bakedModel, bufferSource, partialTick);

		}

	}

}
