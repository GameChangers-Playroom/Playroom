package io.github.flameyheart.playroom.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class CustomRenderLayer {

	private static final Function<Identifier, RenderLayer> ENTITY_GLOWING = Util.memoize(texture -> 
		RenderLayer.of("entity_glowing", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS,
					256, false, true, RenderLayer.MultiPhaseParameters.builder()
																											.program(RenderLayer.ENTITY_ALPHA_PROGRAM)
																											.texture(new RenderPhase.Texture(texture, false, false))
																											.transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY)
																											.writeMaskState(RenderPhase.COLOR_MASK)
																											.build(false))
	);

	private static final Function<Identifier, RenderLayer> ITEM_GLOWING = Util.memoize(textureId -> 
		RenderLayer.of("item_glowing", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS,
					256, false, true, RenderLayer.MultiPhaseParameters.builder()
																											.program(RenderLayer.EYES_PROGRAM)
																											.texture(new RenderPhase.Texture(textureId, false, false))
																											.transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY)
																											.writeMaskState(RenderLayer.COLOR_MASK)
																											.build(false))
	);

	public static RenderLayer getEntityGlowing(Identifier texture) {
		return ENTITY_GLOWING.apply(texture);
	}

	public static RenderLayer getItemGlowing(Identifier texture) {
		return ITEM_GLOWING.apply(texture);
	}

}
