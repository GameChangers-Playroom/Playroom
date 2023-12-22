package io.github.flameyheart.playroom.registry;

import io.github.flameyheart.playroom.Playroom;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

public class Sounds implements AutoRegistryContainer<SoundEvent> {
    public static final SoundEvent LASER_GUN_SHOOT_RANGE = SoundEvent.of(Playroom.id("laser_gun.shoot.range"));
    public static final SoundEvent LASER_GUN_SHOOT_RAPID = SoundEvent.of(Playroom.id("laser_gun.shoot.rapid"));
    public static final SoundEvent LASER_GUN_CHARGE = SoundEvent.of(Playroom.id("laser_gun.charge"));
    public static final SoundEvent FREEZE = SoundEvent.of(Playroom.id("general.freeze"));
    public static final SoundEvent HIT_FREEZE = SoundEvent.of(Playroom.id("general.hit_freeze"));

    @Override
    public Registry<SoundEvent> getRegistry() {
        return Registries.SOUND_EVENT;
    }

    @Override
    public Class<SoundEvent> getTargetFieldType() {
        return AutoRegistryContainer.conform(SoundEvent.class);
    }
}
