package io.github.flameyheart.playroom.render.item.old;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoItemRenderer;

// TODO: Remove
public abstract class AlternativeGeoItemRenderer<T extends Item & GeoAnimatable> extends GeoItemRenderer<T> {

    public AlternativeGeoItemRenderer(AlternativeGeoModel<T> model) {
        super(model);
    }

    protected abstract boolean useAlternativeTexture(T animatable);

    @Override
    public Identifier getTextureLocation(T animatable) {
        if (useAlternativeTexture(animatable)) {
            return ((AlternativeGeoModel<T>) this.model).getAlternativeTextureResource(animatable);
        }
        return super.getTextureLocation(animatable);
    }
}
