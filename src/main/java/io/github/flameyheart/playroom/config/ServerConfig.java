package io.github.flameyheart.playroom.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;
import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.config.annotations.SendToClient;

public class ServerConfig {
    public static final ConfigClassHandler<ServerConfig> INSTANCE = ConfigClassHandler.createBuilder(ServerConfig.class)
        .id(Playroom.id("config"))
        .serializer(config -> GsonConfigSerializerBuilder.create(config)
            .setPath(YACLPlatform.getConfigDir().resolve("playroom/common.json5"))
            .setJson5(true)
            .build()
        ).build();

    public static ServerConfig instance() {
        return INSTANCE.instance();
    }

    public static ServerConfig defaults() {
        return INSTANCE.defaults();
    }

    //region NETWORKING
    @SerialEntry(comment = "Allows vanilla players to join the server")
    public boolean allowVanillaPlayers = false;

    @SerialEntry(comment = "Will kick players with mismatching protocol versions")
    public boolean requireMatchingProtocol = false;

    @SerialEntry(comment = "The port to use for the Tiltify webhook server")
    public short tiltifyWebhookPort = 8443;

    @SerialEntry(comment = "The tiltify webhook secret")
    public String tiltifySecret = "DO NOT SHARE";
    //endregion

    //region LASER GUN
    @SendToClient
    @SerialEntry(comment = "How much amo the rapid fire mode has\n[Min: 1, Max: 127, Default: 5]")
    public byte laserRapidFireAmo = 12;

    @SendToClient
    @SerialEntry(comment = "The laser reload time for the gun cooldown\n[Min: 0, Max: 32767, Default: 3600]")
    public short laserFireReloadTime = 3600;

    @SendToClient
    @SerialEntry(comment = "The laser charge time for the ranged more\n[Min: 0, Max: 32767, Default: 20]")
    public short laserRangeChargeTime = 20;

    @SendToClient
    @SerialEntry(comment = "The cooldown applied to when the player swaps the gun mode\n[Min: 0, Max: 32767, Default: 10]")
    public short laserSwapModeCooldown = 10;

    @SerialEntry(comment = "The speed of the ranged mode ice shot\n[Min: 0.1, Default: 3] [DECIMAL SUPPORTED]")
    public float laserRangedBulletSpeed = 5;

    @SerialEntry(comment = "The divergence of the ranged mode ice shot\n[Min: 0, Default: 0] [DECIMAL SUPPORTED]")
    public float laserRangedDivergence = 0;

    @SerialEntry(comment = "The speed of the rapid fire mode ice shot\n[Min: 0.1, Default: 3] [DECIMAL SUPPORTED]")
    public float laserRapidBulletSpeed = 3;

    @SerialEntry(comment = "The divergence of the rapid fire mode ice shot\n[Min: 0, Default: 0] [DECIMAL SUPPORTED]")
    public float laserRapidDivergence = 0;
    //endregion

    //region FREEZE TIMES
    @SendToClient
    @SerialEntry(comment = "The duration of the zoom effect\n[Min: 0, Default: 60]")
    public int freezeZoomDuration = 60;

    @SendToClient
    @SerialEntry(comment = "The target FOV for the zoom effect\n[Min: 0, Default: 0.1] [DECIMAL SUPPORTED]")
    public float freezeZoomFov = 0.1f;

    @SendToClient
    @SerialEntry(comment = "A value to fine tune the zoom animation\n[Min: 0, Default: 5]")
    public int freezeZoomOffset = 5;

    @SendToClient
    @SerialEntry(comment = "The duration of the slowdown effect\n[Min: 0, Default: ??]")
    public int freezeSlowdownTime = 50;

    @SendToClient
    @SerialEntry(comment = "The duration of the ice\n[Min: 0, Default: ??]")
    public int freezeIceTime = 100;
    //endregion
}
