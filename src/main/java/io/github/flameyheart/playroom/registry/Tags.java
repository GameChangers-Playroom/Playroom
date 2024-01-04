package io.github.flameyheart.playroom.registry;

import io.github.flameyheart.playroom.Playroom;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class Tags {
    public static final TagKey<Item> CRUNCHY_CRYSTALS = TagKey.of(RegistryKeys.ITEM, Playroom.id("crunchy_crystals"));
    public static final TagKey<EntityType<?>> IMMUNE_TO_FREEZE = TagKey.of(RegistryKeys.ENTITY_TYPE, Playroom.id("immune_to_freeze"));
    public static final TagKey<DamageType> NO_TILT = TagKey.of(RegistryKeys.DAMAGE_TYPE, Playroom.id("no_tilt"));
    public static final TagKey<DamageType> NO_HURT_SOUND = TagKey.of(RegistryKeys.DAMAGE_TYPE, Playroom.id("no_hurt_sound"));
}
