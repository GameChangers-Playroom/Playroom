package io.github.flameyheart.playroom.render.item.old;

import io.github.flameyheart.playroom.mixin.client.geo.AutoGlowingTextureAccessor;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;

// TODO: Remove
public class AnimatedAutoGlowingTexture extends AutoGlowingTexture {
    protected static final String APPENDIX = "_glowmask";

    public AnimatedAutoGlowingTexture(Identifier originalLocation, Identifier location) {
        super(originalLocation, location);
    }

    @NotNull
    protected static Identifier getEmissiveResource(Identifier baseResource) {
        return appendToPath(baseResource, APPENDIX);
    }

    public static RenderLayer getRenderType(Identifier texture) {
        return AutoGlowingTextureAccessor.getRENDER_TYPE_FUNCTION().apply(getEmissiveResource(texture));
    }
}
