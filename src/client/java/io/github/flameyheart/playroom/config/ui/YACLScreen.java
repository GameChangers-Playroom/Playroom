package io.github.flameyheart.playroom.config.ui;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.SliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import io.github.flameyheart.playroom.config.ClientConfig;
import io.github.flameyheart.playroom.config.ServerConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class YACLScreen {
    public static Screen createScreen(Screen parent) {
        ServerConfig serverConfig = ServerConfig.instance();
        ServerConfig serverDefaults = ServerConfig.defaults();

        ClientConfig clientConfig = ClientConfig.instance();
        ClientConfig clientDefaults = ClientConfig.defaults();

        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder();
        builder.title(Text.literal("Used for narration. Could be used to render a title in the future."));

        ConfigCategory.Builder general = ConfigCategory.createBuilder()
            .name(Text.translatable("config.playroom.category.general"));

        ConfigCategory.Builder networking = ConfigCategory.createBuilder()
            .name(Text.translatable("config.playroom.category.networking"));

        networking.option(
            Option.<Boolean>createBuilder()
                .name(Text.translatable("config.playroom.option.networking.allowVanillaPlayers"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.networking.allowVanillaPlayers.description")))
                .binding(serverDefaults.allowVanillaPlayers, () -> serverConfig.allowVanillaPlayers, newVal -> serverConfig.allowVanillaPlayers = newVal)
                .controller(TickBoxControllerBuilder::create)
                .build()
        );

        networking.option(
            Option.<Boolean>createBuilder()
                .name(Text.translatable("config.playroom.option.networking.requireMatchingProtocol"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.networking.requireMatchingProtocol.description")))
                .binding(serverDefaults.requireMatchingProtocol, () -> serverConfig.requireMatchingProtocol, newVal -> serverConfig.requireMatchingProtocol = newVal)
                .controller(TickBoxControllerBuilder::create)
                .build()
        );

        ConfigCategory.Builder server = ConfigCategory.createBuilder()
          .name(Text.translatable("config.playroom.category.server"));

        builder.category(general.build());
        builder.category(networking.build());

        return builder.build().generateScreen(parent);
    }
}
