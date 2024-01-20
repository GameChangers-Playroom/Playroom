package io.github.flameyheart.playroom.tiltify;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ExecuteAction(@NotNull Automation.Task<?> task, @Nullable ServerPlayerEntity player, String donorName) {
}
