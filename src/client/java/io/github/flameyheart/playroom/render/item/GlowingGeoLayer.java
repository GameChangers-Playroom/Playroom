package io.github.flameyheart.playroom.render.item;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.function.Function;

public class GlowingGeoLayer<T extends GeoItem> extends GeoRenderLayer<T> {

	protected static final Function<Identifier, RenderLayer> GLOWING = Util.memoize(texture -> {
		RenderPhase.Texture texture2 = new RenderPhase.Texture(texture, false, false);
		return RenderLayer.of("eyes", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, false, true, RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.EYES_PROGRAM).texture(texture2).transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY).writeMaskState(RenderLayer.COLOR_MASK).build(false));
	});

	protected float layerAlpha = 1;
	protected float layerAlphaMultiplier = 1;

	public GlowingGeoLayer(GeoRenderer<T> renderer) {
		super(renderer);
	}

	protected RenderLayer getRenderType(T animatable) {
		return GLOWING.apply(getTextureResource(renderer.getAnimatable()));
	}

	public void render(MatrixStack poseStack, T animatable, BakedGeoModel bakedModel, VertexConsumerProvider bufferSource, float partialTick) {
		RenderLayer emissiveRenderType = getRenderType(animatable);

		if(layerAlpha == 0)
			return;

		getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveRenderType, bufferSource.getBuffer(emissiveRenderType), partialTick, 0xF0, 0xA0000, 1, 1, 1, layerAlpha * layerAlphaMultiplier);

	}

	@Override
	public void render(MatrixStack poseStack, T animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
		throw new AssertionError("Method do not use!");
	}

}
