package io.github.flameyheart.playroom.render.item;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.item.LaserGun;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class LaserGunModel extends AlternativeGeoModel<LaserGun> {
    @Override
    public Identifier getModelResource(LaserGun animatable) {
        return Playroom.id("geo/laser_gun.geo.json");
    }

    @Override
    public Identifier getTextureResource(LaserGun animatable) {
        return Playroom.id("textures/item/laser_gun.png");
    }

    @Override
    public Identifier getAlternativeTextureResource(LaserGun animatable) {
        return Playroom.id("textures/item/rapid_laser_gun.png");
    }

    @Override
    public Identifier getAnimationResource(LaserGun animatable) {
        return Playroom.id("animations/laser_gun.animation.json");
    }
}
