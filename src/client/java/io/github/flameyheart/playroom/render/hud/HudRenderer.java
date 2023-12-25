package io.github.flameyheart.playroom.render.hud;

import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.config.ClientConfig;
import io.github.flameyheart.playroom.config.ServerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import software.bernie.geckolib.util.RenderUtils;

import java.util.Arrays;

public class HudRenderer {

    public static void render(DrawContext drawContext) {
        if (!ClientConfig.instance().debugInfo) return;
        //MatrixStack matrixStack = drawContext.getMatrices();
        //MinecraftClient client = MinecraftClient.getInstance();
        //Renderer2d.renderRoundedQuad(drawContext.getMatrices(), Color.RED, 10, 10, 100, 100, 5, 5);
        //new Flat(drawContext, 1, new Box(10, 10, 100, 100)).new Oval(Palette.RED).addColor(1, Palette.CYAN).addColor(2, Palette.CYAN).render();
        drawContext.drawText(MinecraftClient.getInstance().textRenderer, "Use ticks: " + (MinecraftClient.getInstance().player == null ? "Null player" : MinecraftClient.getInstance().player.getItemUseTimeLeft()), 5, 5, 0xFFFFFF, true);
        drawContext.drawText(MinecraftClient.getInstance().textRenderer, "Frozen model: " + PlayroomClient.frozenModel.size(), 5, 16, 0xFFFFFF, true);
        //matrixStack.push();
        //matrixStack.translate(100, 100, 0);
        //if (client.world != null) {
        //    matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(client.world.getTime() * 4 % 90));
        //}
        //matrixStack.translate(-100, -100, 0);
        //Renderer2d.renderQuad(matrixStack, Color.RED, 50, 50, 150, 150);
        //matrixStack.pop();
    }
}
