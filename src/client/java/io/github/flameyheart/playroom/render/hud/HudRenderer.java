package io.github.flameyheart.playroom.render.hud;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.config.ClientConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class HudRenderer {

    public static void renderDebugInfo(DrawContext drawContext) {
        if (!ClientConfig.instance().debugInfo || MinecraftClient.getInstance().options.debugEnabled) return;
        drawContext.drawText(MinecraftClient.getInstance().textRenderer, "Server time: " + Playroom.serverTime, 5, 5, 0xFFFFFF, true);
        drawContext.drawText(MinecraftClient.getInstance().textRenderer, "Zooming: " + PlayroomClient.isZooming(), 5, 16, 0xFFFFFF, true);
        drawContext.drawText(MinecraftClient.getInstance().textRenderer, "Zoom divisor: " + PlayroomClient.getPreviousZoomDivisor(), 5, 27, 0xFFFFFF, true);
    }
}
