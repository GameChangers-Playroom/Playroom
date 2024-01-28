package io.github.flameyheart.playroom.mixin;

import io.github.flameyheart.playroom.duck.FreezableEntity;
import io.github.flameyheart.playroom.item.LaserGun;
import io.github.flameyheart.playroom.item.OldLaserGun;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private boolean disableRegen(GameRules instance, GameRules.Key<GameRules.BooleanRule> rule, PlayerEntity player) {
        if (((FreezableEntity) player).playroom$isFrozen() || (((PlayerEntityAccessor) player).getSelectedItem().getItem() instanceof LaserGun || ((PlayerEntityAccessor) player).getSelectedItem().getItem() instanceof OldLaserGun)) {
            return false;
        }
        return instance.getBoolean(rule);
    }

}
