package io.github.flameyheart.playroom.mixin.client.geo;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;

import java.util.function.Function;

// TODO: Remove
@Mixin(value = AutoGlowingTexture.class, remap = false)
public interface AutoGlowingTextureAccessor {
    @Accessor static Function<Identifier, RenderLayer> getRENDER_TYPE_FUNCTION() { throw new AssertionError(); }
    @Accessor static RenderPhase.Transparency getTRANSPARENCY_STATE() { throw new AssertionError(); }
    @Accessor static RenderPhase.WriteMaskState getWRITE_MASK() { throw new AssertionError(); }
}
