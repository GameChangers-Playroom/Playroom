package io.github.flameyheart.playroom.render.hud;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.config.ClientConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib.util.RenderUtils;

public class HudRenderer {
    
    public static float animFrameTick;
    public static float chargeLayerAlpha;

    public static void renderDebugInfo(DrawContext drawContext) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!ClientConfig.instance().debugInfo || client.options.debugEnabled) return;
        drawContext.drawText(client.textRenderer, "Server time: " + Playroom.serverTime, 5, 5, 0xFFFFFF, true);
        drawContext.drawText(client.textRenderer, "Aim zoom: " + PlayroomClient.hasAimZoom(), 5, 16, 0xFFFFFF, true);
        drawContext.drawText(client.textRenderer, "Unfreeze zoom: " + PlayroomClient.hasUnfreezeZoom(), 5, 27, 0xFFFFFF, true);
        drawContext.drawText(client.textRenderer, "Aim zoom divisor: " + PlayroomClient.getPreviousAimZoomDivisor(), 5, 38, 0xFFFFFF, true);
        drawContext.drawText(client.textRenderer, "Unfreeze zoom divisor: " + PlayroomClient.getPreviousUnfreezeZoomDivisor(), 5, 49, 0xFFFFFF, true);
        drawContext.drawText(client.textRenderer, "Render Frame Tick (RFT): " + (int) RenderUtils.getCurrentTick(), 5, 60, 0xFFFFFF, true);
        drawContext.drawText(client.textRenderer, "Animation Frame Tick (AFT): " + animFrameTick, 5, 71, 0xFFFFFF, true);
        drawContext.drawText(client.textRenderer, "Alpha (Layer: ChargeLayer): " + chargeLayerAlpha, 5, 82, 0xFFFFFF, true);
        Entity target = client.targetedEntity;
        String content;
        if (target instanceof PlayerEntity targetPlayer) {
            ItemStack stack = targetPlayer.getMainHandStack();
            if (!stack.isEmpty() && stack.hasNbt() && stack.getNbt().contains("Playroom")) {
                content = stack.getNbt().get("Playroom").toString();
            } else if (!stack.isEmpty() && stack.hasNbt()) {
                content = "No Playroom data";
            } else if (!stack.isEmpty()) {
                content = "No NBT";
            } else {
                content = "No item";
            }
        } else if (target != null) {
            content = "Targeted entity: " + target.getType().getTranslationKey();
        } else {
            content = "No targeted entity";
        }
        drawContext.drawText(client.textRenderer, "Target: " + content, 5, 93, 0xFFFFFF, true);
    }
}
