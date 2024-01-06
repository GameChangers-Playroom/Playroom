package io.github.flameyheart.playroom.config.ui;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.config.ClientConfig;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.zoom.TransitionType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
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
          Option.<Boolean>createBuilder()
            .name(Text.translatable("config.playroom.option.general.reducedMotion"))
            .description(OptionDescription.of(Text.translatable("config.playroom.option.general.reducedMotion.description")))
            .binding(clientDefaults.reducedMotion, () -> clientConfig.reducedMotion, newVal -> clientConfig.reducedMotion = newVal)
            .controller(TickBoxControllerBuilder::create)
            .build()
        );

        general.option(
          Option.<Boolean>createBuilder()
            .name(Text.translatable("config.playroom.option.general.debugInfo"))
            .description(OptionDescription.of(Text.translatable("config.playroom.option.general.debugInfo.description")))
            .binding(clientDefaults.debugInfo, () -> clientConfig.debugInfo, newVal -> clientConfig.debugInfo = newVal)
            .controller(TickBoxControllerBuilder::create)
            .build()
        );

        general.option(
          Option.<Integer>createBuilder()
            .name(Text.translatable("config.playroom.option.general.laserAimCameraSmoothness"))
            .description(OptionDescription.of(Text.translatable("config.playroom.option.general.laserAimCameraSmoothness.description")))
            .binding((int) clientDefaults.laserAimCameraSmoothness, () -> (int) clientConfig.laserAimCameraSmoothness, newVal -> clientConfig.laserAimCameraSmoothness = newVal.shortValue())
            .controller(option -> IntegerSliderControllerBuilder.create(option).range(0, 150).formatValue(value -> Text.literal("%d%%".formatted(value))).step(1))
            .build()
        );

        general.option(
          Option.<Double>createBuilder()
            .name(Text.translatable("config.playroom.option.general.laserAimZoomInTime"))
            .description(OptionDescription.of(Text.translatable("config.playroom.option.general.laserAimZoomInTime.description")))
            .binding(clientDefaults.laserAimZoomInTime, () -> clientConfig.laserAimZoomInTime, newVal -> clientConfig.laserAimZoomInTime = newVal)
            .controller(option -> DoubleSliderControllerBuilder.create(option).range(0d, 3d).formatValue(value -> Text.translatable("zoomify.gui.formatter.seconds", "%.1f".formatted(value))).step(0.1))
            .build()
        );

        general.option(
          Option.<Double>createBuilder()
            .name(Text.translatable("config.playroom.option.general.laserAimZoomOutTime"))
            .description(OptionDescription.of(Text.translatable("config.playroom.option.general.laserAimZoomOutTime.description")))
            .binding(clientDefaults.laserAimZoomOutTime, () -> clientConfig.laserAimZoomOutTime, newVal -> clientConfig.laserAimZoomOutTime = newVal)
            .controller(option -> DoubleSliderControllerBuilder.create(option).range(0d, 3d).formatValue(value -> Text.translatable("zoomify.gui.formatter.seconds", "%.1f".formatted(value))).step(0.1))
            .build()
        );

        general.option(
          Option.<TransitionType>createBuilder()
            .name(Text.translatable("config.playroom.option.general.laserAimZoomInTransition"))
            .description(OptionDescription.of(Text.translatable("config.playroom.option.general.laserAimZoomInTransition.description")))
            .binding(clientDefaults.laserAimZoomInTransition, () -> clientConfig.laserAimZoomInTransition, newVal -> clientConfig.laserAimZoomInTransition = newVal)
            .controller(option -> EnumControllerBuilder.create(option).enumClass(TransitionType.class).formatValue(value -> Text.translatable(value.getDisplayName())))
            .build()
        );

        general.option(
          Option.<TransitionType>createBuilder()
            .name(Text.translatable("config.playroom.option.general.laserAimZoomOutTransition"))
            .description(OptionDescription.of(Text.translatable("config.playroom.option.general.laserAimZoomOutTransition.description")))
            .binding(clientDefaults.laserAimZoomOutTransition, () -> clientConfig.laserAimZoomOutTransition.opposite(), newVal -> clientConfig.laserAimZoomOutTransition = newVal.opposite())
            .controller(option -> EnumControllerBuilder.create(option).enumClass(TransitionType.class).formatValue(value -> Text.translatable(value.getDisplayName())))
            .build()
        );

        builder.category(general.build());

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || Permissions.check(player, "playroom.config.server", 4)) {
            ConfigCategory.Builder server = ConfigCategory.createBuilder().name(Text.translatable("config.playroom.category.server"));

            //region Networking
            OptionGroup.Builder networking = OptionGroup.createBuilder().name(Text.translatable("config.playroom.option_group.networking"));
            networking.collapsed(true);

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

            networking.option(
              Option.<Boolean>createBuilder()
                .name(Text.translatable("config.playroom.option.networking.requireMatchingVersion"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.networking.requireMatchingVersion.description")))
                .binding(serverDefaults.requireMatchingVersion, () -> serverConfig.requireMatchingVersion, newVal -> serverConfig.requireMatchingVersion = newVal)
                .controller(TickBoxControllerBuilder::create)
                .build()
            );

            networking.option(
              Option.<Integer>createBuilder()
                .name(Text.translatable("config.playroom.option.networking.tiltifyWebhookPort"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.networking.tiltifyWebhookPort.description")))
                .binding(serverDefaults.tiltifyWebhookPort, () -> serverConfig.tiltifyWebhookPort, newVal -> serverConfig.tiltifyWebhookPort = newVal.shortValue())
                .controller(option -> IntegerFieldControllerBuilder.create(option).min(0).max(32767))
                .build()
            );

            server.group(networking.build());
            //endregion
            //region Laser Gun
            OptionGroup.Builder laserGun = OptionGroup.createBuilder().name(Text.translatable("config.playroom.option_group.laser_gun"));
            laserGun.collapsed(true);

            laserGun.option(
              Option.<Integer>createBuilder()
                .name(Text.translatable("config.playroom.option.laser_gun.laserRapidFireAmo"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.laser_gun.laserRapidFireAmo.description")))
                .binding((int) serverDefaults.laserRapidFireAmo, () -> (int) serverConfig.laserRapidFireAmo, newVal -> serverConfig.laserRapidFireAmo = newVal.byteValue())
                .controller(option -> IntegerSliderControllerBuilder.create(option).range(1, 20).step(1))
                .build()
            );

            laserGun.option(
              Option.<Integer>createBuilder()
                .name(Text.translatable("config.playroom.option.laser_gun.laserFireReloadTime"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.laser_gun.laserFireReloadTime.description")))
                .binding((int) serverDefaults.laserFireReloadTime, () -> (int) serverConfig.laserFireReloadTime, newVal -> serverConfig.laserFireReloadTime = newVal.shortValue())
                .controller(option -> IntegerSliderControllerBuilder.create(option).range(0, 3600).step(20))
                .build()
            );

            laserGun.option(
              Option.<Integer>createBuilder()
                .name(Text.translatable("config.playroom.option.laser_gun.laserRangeChargeTime"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.laser_gun.laserRangeChargeTime.description")))
                .binding((int) serverDefaults.laserRangeChargeTime, () -> (int) serverConfig.laserRangeChargeTime, newVal -> serverConfig.laserRangeChargeTime = newVal.shortValue())
                .controller(option -> IntegerSliderControllerBuilder.create(option).range(0, 20).step(1))
                .build()
            );

            laserGun.option(
              Option.<Integer>createBuilder()
                .name(Text.translatable("config.playroom.option.laser_gun.laserRapidFireCooldown"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.laser_gun.laserRapidFireCooldown.description")))
                .binding((int) serverDefaults.laserRapidFireCooldown, () -> (int) serverConfig.laserRapidFireCooldown, newVal -> serverConfig.laserRapidFireCooldown = newVal.shortValue())
                .controller(option -> IntegerSliderControllerBuilder.create(option).range(0, 200).step(5))
                .build()
            );

            laserGun.option(
              Option.<Integer>createBuilder()
                .name(Text.translatable("config.playroom.option.laser_gun.laserSwapModeCooldown"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.laser_gun.laserSwapModeCooldown.description")))
                .binding((int) serverDefaults.laserSwapModeCooldown, () -> (int) serverConfig.laserSwapModeCooldown, newVal -> serverConfig.laserSwapModeCooldown = (short) newVal.intValue())
                .controller(option -> IntegerSliderControllerBuilder.create(option).range(0, 2400).step(20))
                .build()
            );

            laserGun.option(
              Option.<Float>createBuilder()
                .name(Text.translatable("config.playroom.option.laser_gun.laserRangedBulletSpeed"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.laser_gun.laserRangedBulletSpeed.description")))
                .binding(serverDefaults.laserRangedBulletSpeed, () -> serverConfig.laserRangedBulletSpeed, newVal -> serverConfig.laserRangedBulletSpeed = newVal)
                .controller(option -> FloatSliderControllerBuilder.create(option).range(0.1f, 30f).step(0.1f))
                .build()
            );

            laserGun.option(
              Option.<Float>createBuilder()
                .name(Text.translatable("config.playroom.option.laser_gun.laserRangedDivergence"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.laser_gun.laserRangedDivergence.description")))
                .binding(serverDefaults.laserRangedDivergence, () -> serverConfig.laserRangedDivergence, newVal -> serverConfig.laserRangedDivergence = newVal)
                .controller(option -> FloatSliderControllerBuilder.create(option).range(0f, 10f).step(0.02f))
                .build()
            );

            laserGun.option(
              Option.<Float>createBuilder()
                .name(Text.translatable("config.playroom.option.laser_gun.laserRapidBulletSpeed"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.laser_gun.laserRapidBulletSpeed.description")))
                .binding(serverDefaults.laserRapidBulletSpeed, () -> serverConfig.laserRapidBulletSpeed, newVal -> serverConfig.laserRapidBulletSpeed = newVal)
                .controller(option -> FloatSliderControllerBuilder.create(option).range(0.1f, 30f).step(0.1f))
                .build()
            );

            laserGun.option(
              Option.<Float>createBuilder()
                .name(Text.translatable("config.playroom.option.laser_gun.laserRapidDivergence"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.laser_gun.laserRapidDivergence.description")))
                .binding(serverDefaults.laserRapidDivergence, () -> serverConfig.laserRapidDivergence, newVal -> serverConfig.laserRapidDivergence = newVal)
                .controller(option -> FloatSliderControllerBuilder.create(option).range(0f, 10f).step(0.02f))
                .build()
            );

            laserGun.option(
              Option.<Integer>createBuilder()
                .name(Text.translatable("config.playroom.option.laser_gun.laserRapidFreezeAmount"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.laser_gun.laserRapidFreezeAmount.description")))
                .binding((int) serverDefaults.laserRapidFreezeAmount, () -> (int) serverConfig.laserRapidFreezeAmount, newVal -> serverConfig.laserRapidFreezeAmount = newVal.shortValue())
                .controller(option -> IntegerSliderControllerBuilder.create(option).range(0, 500).step(20))
                .build()
            );

            laserGun.option(
              Option.<Float>createBuilder()
                .name(Text.translatable("config.playroom.option.laser_gun.laserAimSlowdown"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.laser_gun.laserAimSlowdown.description")))
                .binding(serverDefaults.laserAimSlowdown, () -> serverConfig.laserAimSlowdown, newVal -> serverConfig.laserAimSlowdown = newVal)
                .controller(option -> FloatSliderControllerBuilder.create(option).range(0f, 1f).step(0.05f))
                .build()
            );

            laserGun.option(
              Option.<Integer>createBuilder()
                .name(Text.translatable("config.playroom.option.laser_gun.laserAimZoom"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.laser_gun.laserAimZoom.description")))
                .binding(serverDefaults.laserAimZoom, () -> serverConfig.laserAimZoom, newVal -> serverConfig.laserAimZoom = newVal)
                .controller(option -> IntegerSliderControllerBuilder.create(option).range(1, 10).formatValue(value -> Text.literal("%dx".formatted(value))).step(1))
                .build()
            );

            laserGun.option(
              Option.<Float>createBuilder()
                .name(Text.translatable("config.playroom.option.laser_gun.laserRangeDamage"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.laser_gun.laserRangeDamage.description")))
                .binding(serverDefaults.laserRangeDamage, () -> serverConfig.laserRangeDamage, newVal -> serverConfig.laserRangeDamage = newVal)
                .controller(option -> FloatSliderControllerBuilder.create(option).range(0f, 5f)
                  .formatValue(value -> Text.literal(String.format("%,.1f ❤", value).replaceAll("[\u00a0\u202F]", " ")))
                  .step(0.01f))
                .build()
            );

            laserGun.option(
              Option.<Float>createBuilder()
                .name(Text.translatable("config.playroom.option.laser_gun.laserRapidDamage"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.laser_gun.laserRapidDamage.description")))
                .binding(serverDefaults.laserRapidDamage, () -> serverConfig.laserRapidDamage, newVal -> serverConfig.laserRapidDamage = newVal)
                .controller(option -> FloatSliderControllerBuilder.create(option).range(0f, 5f)
                  .formatValue(value -> Text.literal(String.format("%,.1f ❤", value).replaceAll("[\u00a0\u202F]", " ")))
                  .step(0.01f))
                .build()
            );

            server.group(laserGun.build());
            //endregion
            //region Player Freeze
            OptionGroup.Builder playerFreeze = OptionGroup.createBuilder().name(Text.translatable("config.playroom.option_group.player_freeze"));
            playerFreeze.collapsed(true);

            playerFreeze.option(
              Option.<Float>createBuilder()
                .name(Text.translatable("config.playroom.option.player_freeze.freezeFallDamageReduction"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.player_freeze.freezeFallDamageReduction.description")))
                .binding(serverDefaults.freezeFallDamageReduction, () -> serverConfig.freezeFallDamageReduction, newVal -> serverConfig.freezeFallDamageReduction = newVal)
                .controller(option -> FloatSliderControllerBuilder.create(option).range(0f, 1f).step(0.01f))
                .build()
            );

            playerFreeze.option(
              Option.<Float>createBuilder()
                .name(Text.translatable("config.playroom.option.player_freeze.freezeFallIceDamage"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.player_freeze.freezeFallIceDamage.description")))
                .binding(serverDefaults.freezeFallIceDamage, () -> serverConfig.freezeFallIceDamage, newVal -> serverConfig.freezeFallIceDamage = newVal)
                .controller(option -> FloatSliderControllerBuilder.create(option).range(0f, 1f).step(0.01f))
                .build()
            );

            playerFreeze.option(
              Option.<Integer>createBuilder()
                .name(Text.translatable("config.playroom.option.player_freeze.freezeSlowdownTime"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.player_freeze.freezeSlowdownTime.description")))
                .binding(serverDefaults.freezeSlowdownTime, () -> serverConfig.freezeSlowdownTime, newVal -> serverConfig.freezeSlowdownTime = newVal)
                .controller(option -> IntegerSliderControllerBuilder.create(option).range(0, 2400).step(10))
                .build()
            );

            playerFreeze.option(
              Option.<Float>createBuilder()
                .name(Text.translatable("config.playroom.option.player_freeze.freezeSlowdown"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.player_freeze.freezeSlowdown.description")))
                .binding(serverDefaults.freezeSlowdown, () -> serverConfig.freezeSlowdown, newVal -> serverConfig.freezeSlowdown = newVal)
                .controller(option -> FloatSliderControllerBuilder.create(option).range(0f, 1f).step(0.01f))
                .build()
            );

            playerFreeze.option(
              Option.<Integer>createBuilder()
                .name(Text.translatable("config.playroom.option.player_freeze.freezeIceTime"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.player_freeze.freezeIceTime.description")))
                .binding(serverDefaults.freezeIceTime, () -> serverConfig.freezeIceTime, newVal -> serverConfig.freezeIceTime = newVal)
                .controller(option -> IntegerSliderControllerBuilder.create(option).range(0, 2400).step(10))
                .build()
            );

            /*playerFreeze.option(
              Option.<Float>createBuilder()
                .name(Text.translatable("config.playroom.option.player_freeze.freezeSlowdown"))
                .description(OptionDescription.of(Text.translatable("config.playroom.option.player_freeze.freezeSlowdown.description")))
                .binding(serverDefaults.freezeSlowdown, () -> serverConfig.freezeSlowdown, newVal -> serverConfig.freezeSlowdown = newVal)
                .controller(option -> FloatSliderControllerBuilder.create(option).range(0f, 1f).step(0.05f))
                .build()
            );*/

            server.group(playerFreeze.build());
            //endregion
            server.option(LabelOption.create(Text.translatable("config.playroom.option.server.commands")));

            builder.category(server.build());
        }

        builder.save(() -> {
            ClientConfig.INSTANCE.save();
            if (Playroom.getServer() != null && Playroom.getServer().isSingleplayer()) {
                ServerConfig.INSTANCE.save();
            } else {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeString(Playroom.serializeConfig(true));
                ClientPlayNetworking.send(Playroom.id("config/update"), buf);
            }
        });
        return builder.build().generateScreen(parent);
    }
}
