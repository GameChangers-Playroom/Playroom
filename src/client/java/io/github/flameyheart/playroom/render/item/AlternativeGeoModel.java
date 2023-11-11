package io.github.flameyheart.playroom.render.item;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public abstract class AlternativeGeoModel<T extends GeoAnimatable> extends GeoModel<T> {

    public abstract Identifier getAlternativeTextureResource(T animatable);
}
