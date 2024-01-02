package io.github.flameyheart.playroom;

import net.minecraft.entity.EntityType;

import java.util.List;

public class ClientConstants {
    public static final List<EntityType<?>> IGNORE_ICE_SCALE = List.of(
      EntityType.ELDER_GUARDIAN,
      EntityType.WITHER
    );
}
