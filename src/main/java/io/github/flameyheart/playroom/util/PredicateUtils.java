package io.github.flameyheart.playroom.util;

import io.github.flameyheart.playroom.Playroom;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class PredicateUtils {
    public static Predicate<Entity> permission(String permission, int level) {
        return player -> Permissions.check(player, permission, level);
    }

    public static boolean checkUnlessDev(@Nullable Entity entity, @NotNull String permission, int defaultRequiredLevel, boolean devValue) {
        if (entity == null) return false;
        if (Playroom.isDev()) return devValue;
        return Permissions.check(entity.getCommandSource(), permission, defaultRequiredLevel);
    }
}
