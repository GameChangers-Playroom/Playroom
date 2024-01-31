package io.github.flameyheart.playroom.render.item;

import io.github.flameyheart.playroom.render.CustomRenderLayer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class GlowingGeoLayer<T extends GeoItem> extends GeoRenderLayer<T> {

	protected float layerAlpha = 1;
	protected float layerAlphaMultiplier = 1;

	public GlowingGeoLayer(GeoRenderer<T> renderer) {
		super(renderer);
	}

	protected RenderLayer getRenderType(T animatable) {
		return CustomRenderLayer.getItemGlowing(getTextureResource(renderer.getAnimatable()));
	}

	public void render(MatrixStack poseStack, T animatable, BakedGeoModel bakedModel, VertexConsumerProvider bufferSource, float partialTick) {
		
		if(layerAlpha == 0)
			return;
		
		RenderLayer emissiveRenderType = getRenderType(animatable);
		getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveRenderType, bufferSource.getBuffer(emissiveRenderType), partialTick, 0xF0, 0xA0000, 1, 1, 1, layerAlpha * layerAlphaMultiplier);
	
	}

	@Override
	public void render(MatrixStack poseStack, T animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
		render(poseStack, animatable, bakedModel, bufferSource, partialTick);
	}

}
