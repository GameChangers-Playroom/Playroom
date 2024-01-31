package io.github.flameyheart.playroom.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class CustomRenderLayer {

	private static final Function<Identifier, RenderLayer> ENTITY_GLOWING = Util.memoize(texture -> {
		RenderPhase.Texture texture2 = new RenderPhase.Texture(texture, false, false);
		return RenderLayer.of("eyes", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS,
					256, false, true, RenderLayer.MultiPhaseParameters.builder()
																											.program(RenderLayer.EYES_PROGRAM)
																											.texture(texture2)
																											.transparency(RenderLayer.LIGHTNING_TRANSPARENCY)
																											.writeMaskState(RenderLayer.COLOR_MASK)
																											.build(false));
	});

	private static final Function<Identifier, RenderLayer> ITEM_GLOWING = Util.memoize(texture -> {
		RenderPhase.Texture texture2 = new RenderPhase.Texture(texture, false, false);
		return RenderLayer.of("glowing", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS,
					256, false, true, RenderLayer.MultiPhaseParameters.builder()
																											.program(RenderLayer.EYES_PROGRAM)
																											.texture(texture2)
																											.transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY)
																											.writeMaskState(RenderLayer.COLOR_MASK)
																											.build(false));
	});

	public static RenderLayer getEntityGlowing(Identifier texture) {
		return ENTITY_GLOWING.apply(texture);
	}

	public static RenderLayer getItemGlowing(Identifier texture) {
		return ITEM_GLOWING.apply(texture);
	}

}
