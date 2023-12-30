package io.github.flameyheart.playroom.registry;

import io.github.flameyheart.playroom.Playroom;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class Tags {
    public static final TagKey<Item> CRUNCHY_CRYSTALS = TagKey.of(RegistryKeys.ITEM, Playroom.id("crunchy_crystals"));
}
