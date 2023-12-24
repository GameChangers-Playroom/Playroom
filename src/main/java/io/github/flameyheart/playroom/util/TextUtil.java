package io.github.flameyheart.playroom.util;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import io.github.flameyheart.playroom.Playroom;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TextUtil {
    public static Text placeholder(Text text) {
        return Placeholders.parseText(text, PlaceholderContext.of(Playroom.getServer()));
    }
    public static Text placeholder(String text) {
        return Placeholders.parseText(Text.literal(text), PlaceholderContext.of(Playroom.getServer()));
    }
    public static Text placeholder(Text text, ServerPlayerEntity player) {
        return Placeholders.parseText(text, PlaceholderContext.of(player));
    }
    public static Text placeholder(String text, ServerPlayerEntity player) {
        return Placeholders.parseText(Text.literal(text), PlaceholderContext.of(player));
    }
}
