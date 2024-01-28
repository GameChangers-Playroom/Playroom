package io.github.flameyheart.playroom.render.item.old;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

// TODO: Remove
public abstract class AlternativeGeoModel<T extends GeoAnimatable> extends GeoModel<T> {

    public abstract Identifier getAlternativeTextureResource(T animatable);
}
