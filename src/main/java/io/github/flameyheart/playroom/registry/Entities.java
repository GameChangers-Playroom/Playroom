package io.github.flameyheart.playroom.registry;

import io.github.flameyheart.playroom.entity.LaserProjectileEntity;
import io.wispforest.owo.registration.reflect.EntityRegistryContainer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;

public class Entities implements EntityRegistryContainer {
    public static final EntityType<LaserProjectileEntity> LASER_SHOT = EntityType.Builder.<LaserProjectileEntity>create(LaserProjectileEntity::new, SpawnGroup.MISC).setDimensions(0.25f, 0.25f).maxTrackingRange(1).build("playroom:laser_shot");
}
