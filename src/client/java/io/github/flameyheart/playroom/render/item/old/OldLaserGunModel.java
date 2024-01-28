package io.github.flameyheart.playroom.render.item.old;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.item.OldLaserGun;
import net.minecraft.util.Identifier;

// TODO: Remove
public class OldLaserGunModel extends AlternativeGeoModel<OldLaserGun> {
    @Override
    public Identifier getModelResource(OldLaserGun animatable) {
        return Playroom.id("geo/laser_gun.geo.old.json");
    }

    @Override
    public Identifier getTextureResource(OldLaserGun animatable) {
        return Playroom.id("textures/item/old/laser_gun.png");
    }

    @Override
    public Identifier getAlternativeTextureResource(OldLaserGun animatable) {
        return Playroom.id("textures/item/old/rapid_laser_gun.png");
    }

    @Override
    public Identifier getAnimationResource(OldLaserGun animatable) {
        return Playroom.id("animations/laser_gun.animation.json");
    }
}
