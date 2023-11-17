package io.github.flameyheart.playroom.registry;

import io.github.flameyheart.playroom.entity.IceSpearEntity;
import io.wispforest.owo.registration.reflect.EntityRegistryContainer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;

public class Entities implements EntityRegistryContainer {
    public static final EntityType<IceSpearEntity> ICE_SPEAR = EntityType.Builder.create(IceSpearEntity::new, SpawnGroup.MISC).setDimensions(0.5f, 0.5f).maxTrackingRange(1).build("playroom:ice_spear");
}
