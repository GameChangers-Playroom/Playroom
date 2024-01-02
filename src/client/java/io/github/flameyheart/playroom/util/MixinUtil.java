package io.github.flameyheart.playroom.util;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Unique;

/**
 * Common methods to be used inside mixins.
 * */
public class MixinUtil {
    public static float playroom$cancelMath(float value, Operation<Float> original, LivingEntity entity) {
        if (entity instanceof ExpandedEntityData eEntity && eEntity.playroom$showIce()) {
            return 0;
        }
        return original.call(value);
    }
}
