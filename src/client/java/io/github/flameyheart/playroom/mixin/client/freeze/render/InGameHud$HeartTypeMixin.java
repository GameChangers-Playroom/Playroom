package io.github.flameyheart.playroom.mixin.client.freeze.render;

import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InGameHud.HeartType.class)
public class InGameHud$HeartTypeMixin {
    @Inject(method = "fromPlayerState", at = @At("HEAD"), cancellable = true)
    private static void changeHeart(PlayerEntity player, CallbackInfoReturnable<InGameHud.HeartType> cir) {
        ExpandedEntityData entity = (ExpandedEntityData) player;

        if (entity.playroom$getGunFreezeTicks() > 0) {
            cir.setReturnValue(InGameHud.HeartType.FROZEN);
        }
    }
}
