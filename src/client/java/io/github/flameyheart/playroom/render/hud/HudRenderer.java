package io.github.flameyheart.playroom.render.hud;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.config.ClientConfig;
import io.github.flameyheart.playroom.util.PredicateUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NbtPredicate;

public class HudRenderer {
    private static int y = 5;

    public static void renderDebugInfo(DrawContext drawContext, float tickDelta) {
        y = 5;
        MinecraftClient client = MinecraftClient.getInstance();
        if (!ClientConfig.instance().debugInfo || client.options.debugEnabled || client.player == null) return;
        if (!PredicateUtils.checkUnlessDev(client.player, "playroom.debug", 4, true)) return;
        drawContext.drawText(client.textRenderer, "Server time: " + Playroom.serverTime, 5, getY(), 0xFFFFFF, true);
        drawContext.drawText(client.textRenderer, "Aim zoom: " + PlayroomClient.hasAimZoom(), 5, getY(), 0xFFFFFF, true);
        drawContext.drawText(client.textRenderer, "Unfreeze zoom: " + PlayroomClient.hasUnfreezeZoom(), 5, getY(), 0xFFFFFF, true);
        drawContext.drawText(client.textRenderer, "Aim zoom divisor: " + PlayroomClient.getPreviousAimZoomDivisor(), 5, getY(), 0xFFFFFF, true);
        drawContext.drawText(client.textRenderer, "Unfreeze zoom divisor: " + PlayroomClient.getPreviousUnfreezeZoomDivisor(), 5, getY(), 0xFFFFFF, true);
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
                content = NbtPredicate.entityToNbt(targetPlayer).get("Playroom").toString();
            }
        } else if (target != null) {
            content = "Targeted entity: " + target.getType().getTranslationKey();
        } else {
            content = "No targeted entity";
        }
        drawContext.drawText(client.textRenderer, "Target: " + content, 5, getY(), 0xFFFFFF, true);
    }

    private static int getY() {
        int v = y;
        y += 11;
        return v;
    }
}
