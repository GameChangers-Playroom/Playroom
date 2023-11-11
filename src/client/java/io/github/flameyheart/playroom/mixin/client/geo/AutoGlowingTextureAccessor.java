package io.github.flameyheart.playroom.mixin.client.geo;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;

import java.util.function.Function;

@Mixin(AutoGlowingTexture.class)
public interface AutoGlowingTextureAccessor {
    @Accessor static Function<Identifier, RenderLayer> getRENDER_TYPE_FUNCTION() {throw new AssertionError();}
}
