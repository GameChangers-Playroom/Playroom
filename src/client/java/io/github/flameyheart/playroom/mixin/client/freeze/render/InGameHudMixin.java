package io.github.flameyheart.playroom.mixin.client.freeze.render;

import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private static Identifier POWDER_SNOW_OUTLINE;

    @Shadow protected abstract void renderOverlay(DrawContext context, Identifier texture, float opacity);

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;getCurrentGameMode()Lnet/minecraft/world/GameMode;", ordinal = 0, shift = At.Shift.BEFORE))
    private void changeHeart(DrawContext context, float tickDelta, CallbackInfo ci) {
        ExpandedEntityData entity = (ExpandedEntityData) this.client.player;

        int freezeTicks = entity.playroom$getGunFreezeTicks();
        if (freezeTicks > 0) {
            this.renderOverlay(context, POWDER_SNOW_OUTLINE, Math.min(1, (freezeTicks) / (float) entity.playroom$slowdownTime()));
        }
    }
}
