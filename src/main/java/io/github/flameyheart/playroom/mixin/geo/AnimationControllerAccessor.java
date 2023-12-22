package io.github.flameyheart.playroom.mixin.geo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

@Mixin(value = AnimationController.class, remap = false)
public interface AnimationControllerAccessor {
    @Accessor RawAnimation getTriggeredAnimation();
}
