package io.github.flameyheart.playroom.util;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.entity.Entity;

import java.util.function.Predicate;

public class PredicateUtils {
    public static Predicate<Entity> permission(String permission, int level) {
        return player -> Permissions.check(player, permission, level);
    }
}
