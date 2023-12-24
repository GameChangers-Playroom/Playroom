package io.github.flameyheart.playroom.util;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.parsers.PatternPlaceholderParser;
import io.github.flameyheart.playroom.Playroom;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class DynamicPlaceholders {
    public static Text parseText(String inputText, PlayerEntity player) {
        return parseText(Text.literal(inputText), player);
    }

    public static Text parseText(Text inputText, PlayerEntity player) {
        Map<String, Text> placeholders = new HashMap<>();
        placeholders.put("player", player.getDisplayName());
        BlockPos blockPos = player.getBlockPos();
        placeholders.put("player_pos", Text.literal(blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ()));

        return Placeholders.parseText(inputText, PlaceholderContext.of(Playroom.getServer()),
          PatternPlaceholderParser.PLACEHOLDER_PATTERN_CUSTOM,
            id -> getPlaceholder(id, placeholders));
    }

    private static PlaceholderHandler getPlaceholder(String id, Map<String, Text> placeholders) {
        return placeholders.containsKey(id) ? (ctx, arg) -> PlaceholderResult.value(placeholders.get(id)) : Placeholders.DEFAULT_PLACEHOLDER_GETTER.getPlaceholder(id);
    }
}
