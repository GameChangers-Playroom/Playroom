package io.github.flameyheart.playroom.config.ui;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import io.github.flameyheart.playroom.config.ServerConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class YACLScreen {
    public static Screen createScreen(Screen parent) {
        ServerConfig config = ServerConfig.instance();
        ServerConfig defaults = ServerConfig.instance();

        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder();
        builder.title(Text.literal("Used for narration. Could be used to render a title in the future."));

        builder.category(ConfigCategory.createBuilder()
            .name(Text.translatable("config.playroom.category.general"))
            .tooltip(Text.translatable("config.playroom.category.general.tooltip"))
            .build()
        );


        ConfigCategory.Builder networking = ConfigCategory.createBuilder()
            .name(Text.translatable("config.playroom.category.networking"))
            .tooltip(Text.translatable("config.playroom.category.networking.tooltip"));

        networking.option(
            Option.<Boolean>createBuilder()
                .name(Text.translatable("config.playroom.networking.allowVanillaPlayers"))
                .description(OptionDescription.of(Text.translatable("config.playroom.networking.allowVanillaPlayers.description")))
                .binding(defaults.allowVanillaPlayers, () -> config.allowVanillaPlayers, newVal -> config.allowVanillaPlayers = newVal)
                .controller(TickBoxControllerBuilder::create)
                .build()
        );

        networking.option(
            Option.<Boolean>createBuilder()
                .name(Text.translatable("config.playroom.networking.requireMatchingProtocol"))
                .description(OptionDescription.of(Text.translatable("config.playroom.networking.requireMatchingProtocol.description")))
                .binding(defaults.requireMatchingProtocol, () -> config.requireMatchingProtocol, newVal -> config.requireMatchingProtocol = newVal)
                .controller(TickBoxControllerBuilder::create)
                .build()
        );

        builder.category(networking.build());

        return builder.build().generateScreen(parent);
    }
}
