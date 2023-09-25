package io.github.flameyheart.playroom.toast;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.flameyheart.playroom.Playroom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;

@Environment(EnvType.CLIENT)
public record WarningToast(Text title, Text message) implements Toast {
    public static final Identifier TEXTURE = Playroom.id("textures/gui/warning_toast.png");

    @Override
    @SuppressWarnings("DuplicatedCode")
    public Visibility draw(DrawContext context, ToastManager manager, long time) {
        TextRenderer textRenderer = manager.getClient().textRenderer;

        int textStart = 24;

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        context.drawTexture(TEXTURE, 0, 0, 0, 0, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
        List<OrderedText> titleList = textRenderer.wrapLines(title, 135);
        List<OrderedText> list = textRenderer.wrapLines(message, 135);
        if (list.size() == 1) {
            context.drawText(textRenderer, title, textStart, 7, 0xFF88FF | 0xFF000000, false);
            context.drawText(textRenderer, list.get(0), textStart, 18, -1, false);
        } else if (list.isEmpty()) {
            context.drawText(textRenderer, title, textStart, 11, 0xFF88FF | 0xFF000000, false);
        } else {
            if (time < 1500L) {
                int k = MathHelper.floor(MathHelper.clamp((float) (1500L - time) / 300.0f, 0.0f, 1.0f) * 255.0f) << 24 | 0x4000000;
                if (titleList.size() <= 1) {
                    context.drawText(textRenderer, title, textStart, 11, 0xFF88FF | k, false);
                } else {
                    int l = this.getHeight() / 2 - Math.min(titleList.size(), 2) * textRenderer.fontHeight / 2;
                    int line = 0;
                    for (OrderedText orderedText : titleList) {
                        if (++line > 2) break;

                        context.drawText(textRenderer, orderedText, textStart, l, 0xFFFFFF | k, false);
                        l += textRenderer.fontHeight;
                    }
                }
            } else {
                int k = MathHelper.floor(MathHelper.clamp((float) (time - 1500L) / 300.0f, 0.0f, 1.0f) * 252.0f) << 24 | 0x4000000;
                int l = this.getHeight() / 2 - Math.min(list.size(), 2) * textRenderer.fontHeight / 2;
                int line = 0;
                for (OrderedText orderedText : list) {
                    if (++line > 2) break;

                    context.drawText(textRenderer, orderedText, textStart, l, 0xFFFFFF | k, false);
                    l += textRenderer.fontHeight;
                }
            }
        }

        return time >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }
}