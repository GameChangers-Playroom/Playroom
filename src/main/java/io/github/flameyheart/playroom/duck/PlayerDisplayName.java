package io.github.flameyheart.playroom.duck;

import net.minecraft.text.Text;

import java.util.UUID;

public record PlayerDisplayName(UUID player, Text prefix, Text displayName) {
}
