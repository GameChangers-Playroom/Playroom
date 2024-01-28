package io.github.flameyheart.playroom.render.item;

import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public abstract class LayeredGeoModel<T extends GeoAnimatable> extends GeoModel<T> {

	public Identifier getLayerTextureResource(T animatable) {
		return getLayerTextureResource(animatable, "default");
	}

	public Identifier getLayerTextureResource(T animatable, String name) {
		return TextureManager.MISSING_IDENTIFIER;
	}

}
