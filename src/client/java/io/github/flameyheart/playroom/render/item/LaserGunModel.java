package io.github.flameyheart.playroom.render.item;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.item.LaserGun;
import net.minecraft.util.Identifier;

public class LaserGunModel extends LayeredGeoModel<LaserGun> {

	public static final String ENABLED_RANGEMODE = "enabledStrips_rangeMode";
	public static final String ENABLED_RAPIDFIREMODE = "enabledStrips_rapidFireMode";
	public static final String MAXSTRIPS = "maxStrips";

	@Override
	public Identifier getLayerTextureResource(LaserGun animatable, String name) {
		
		return switch(name) {
			case ENABLED_RANGEMODE -> Playroom.id("textures/item/range_mode/strips_enabled.png");
			case ENABLED_RAPIDFIREMODE -> Playroom.id("textures/item/rapidfire_mode/strips_enabled.png");
			case MAXSTRIPS -> Playroom.id("textures/item/strips_max.png");
			default -> super.getLayerTextureResource(animatable, name);
		};
	
	}

	@Override
	public Identifier getModelResource(LaserGun animatable) {
		return Playroom.id("geo/laser_gun.geo.json");
	}

	@Override
	public Identifier getTextureResource(LaserGun animatable) {
		return Playroom.id("textures/item/laser_gun.png");
	}

	@Override
	public Identifier getAnimationResource(LaserGun animatable) {
		return Playroom.id("animations/laser_gun.animation.json");
	}

}
