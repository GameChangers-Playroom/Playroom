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

        general.option(
          Option.<Float>createBuilder()
            .name(Text.literal("Value 1"))
            .binding(clientDefaults.value1, () -> clientConfig.value1, newVal -> clientConfig.value1 = newVal)
            .controller(floatOption -> FloatFieldControllerBuilder.create(floatOption)
              .min(-Float.MAX_VALUE)
                .max(Float.MAX_VALUE)
            )
            .build()
        );

        general.option(
          Option.<Float>createBuilder()
            .name(Text.literal("Value 2"))
            .binding(clientDefaults.value2, () -> clientConfig.value2, newVal -> clientConfig.value2 = newVal)
            .controller(floatOption -> FloatFieldControllerBuilder.create(floatOption)
              .min(-Float.MAX_VALUE)
              .max(Float.MAX_VALUE)
            )
            .build()
        );

        general.option(
          Option.<Float>createBuilder()
            .name(Text.literal("Value 3"))
            .binding(clientDefaults.value3, () -> clientConfig.value3, newVal -> clientConfig.value3 = newVal)
            .controller(floatOption -> FloatFieldControllerBuilder.create(floatOption)
              .min(-Float.MAX_VALUE)
              .max(Float.MAX_VALUE)
            )
            .build()
        );

        general.option(
          Option.<Float>createBuilder()
            .name(Text.literal("Value 4"))
            .binding(clientDefaults.value4, () -> clientConfig.value4, newVal -> clientConfig.value4 = newVal)
            .controller(floatOption -> FloatFieldControllerBuilder.create(floatOption)
              .min(-Float.MAX_VALUE)
              .max(Float.MAX_VALUE)
            )
            .build()
        );

        general.option(
          Option.<Float>createBuilder()
            .name(Text.literal("Value 5"))
            .binding(clientDefaults.value5, () -> clientConfig.value5, newVal -> clientConfig.value5 = newVal)
            .controller(floatOption -> FloatFieldControllerBuilder.create(floatOption)
              .min(-Float.MAX_VALUE)
              .max(Float.MAX_VALUE)
            )
            .build()
        );

        general.option(
          Option.<Float>createBuilder()
            .name(Text.literal("Value 6"))
            .binding(clientDefaults.value6, () -> clientConfig.value6, newVal -> clientConfig.value6 = newVal)
            .controller(floatOption -> FloatFieldControllerBuilder.create(floatOption)
              .min(-Float.MAX_VALUE)
              .max(Float.MAX_VALUE)
            )
            .build()
        );

        general.option(
          Option.<Float>createBuilder()
            .name(Text.literal("Value 7"))
            .binding(clientDefaults.value7, () -> clientConfig.value7, newVal -> clientConfig.value7 = newVal)
            .controller(floatOption -> FloatFieldControllerBuilder.create(floatOption)
              .min(-Float.MAX_VALUE)
              .max(Float.MAX_VALUE)
            )
            .build()
        );

        general.option(
          Option.<Float>createBuilder()
            .name(Text.literal("Value 8"))
            .binding(clientDefaults.value8, () -> clientConfig.value8, newVal -> clientConfig.value8 = newVal)
            .controller(floatOption -> FloatFieldControllerBuilder.create(floatOption)
              .min(-Float.MAX_VALUE)
              .max(Float.MAX_VALUE)
            )
            .build()
        );

        general.option(
          Option.<Float>createBuilder()
            .name(Text.literal("Value 9"))
            .binding(clientDefaults.value9, () -> clientConfig.value9, newVal -> clientConfig.value9 = newVal)
            .controller(floatOption -> FloatFieldControllerBuilder.create(floatOption)
              .min(-Float.MAX_VALUE)
              .max(Float.MAX_VALUE)
            )
            .build()
        );

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

        builder.category(general.build());
        builder.category(networking.build());

        return builder.build().generateScreen(parent);
    }
}
