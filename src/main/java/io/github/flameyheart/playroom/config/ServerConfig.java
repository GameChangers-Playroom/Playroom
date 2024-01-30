package io.github.flameyheart.playroom.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;
import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.config.annotations.SendToClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @SerialEntry(comment = "Allows vanilla players to join the server\n[Default: false]")
    public boolean allowVanillaPlayers = false;

    @SerialEntry(comment = "Will kick players with a mismatching protocol version\n[Default: false]")
    public boolean requireMatchingProtocol = true;

    @SerialEntry(comment = "Will kick players with a mismatching mod version\n[Default: false]")
    public boolean requireMatchingVersion = false;

    @SerialEntry(comment = "The port to use for the Tiltify webhook server\n[Min: 0, Max: 2147483647, Default: 8443]")
    public int tiltifyWebhookPort = 8443;

    @SerialEntry(comment = "The tiltify webhook secret\n[Default: \"DO NOT SHARE\"]")
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
    @SerialEntry(comment = "The cooldown between rapidfire automatic shots\n[Min: 0, Max: 32767, Default: 1]")
    public short laserRapidFireCooldown = 1;

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

    @SerialEntry(comment = "The amount of time added per rapid fire shot\n[Min: 0, Max: 32767, Default: 10]")
    public short laserRapidFreezeAmount = 10;

    @SendToClient
    @SerialEntry(comment = "The speed slowdown for when aiming\n0 = 0% | 1 = 100%\n[Min: 0, Default: 0.5, Max: 1] [DECIMAL SUPPORTED]")
    public float laserAimSlowdown = 0.5f;

    @SendToClient
    @SerialEntry(comment = "The player speed multiplier when aiming\n[Min: 1, Default: 4]")
    public int laserAimZoom = 4;

    @SerialEntry(comment = "The amount of damage a ranged shot does\n[Min: 0, Default: 1] [DECIMAL SUPPORTED]")
    public float laserRangeDamage = 1f;

    @SerialEntry(comment = "The amount of damage a rapid fire shot does\n[Min: 0, Default: 0.3] [DECIMAL SUPPORTED]")
    public float laserRapidDamage = 0.3f;

    @SendToClient
    @SerialEntry(comment = "Laser shots can freeze water\n[Default: false]")
    public boolean laserFreezeWater = false;
    //endregion

    //region FREEZE TIMES
    @SerialEntry(comment = "The reduction of the fall damage when a player is frozen\n0 = 0% | 1 = 100%\n[Min: 0, Default: 0.7, Max: 1] [DECIMAL SUPPORTED]")
    public float freezeFallDamageReduction = 0.7f;

    @SerialEntry(comment = "The multiplier of the damage applied to the ice based on the fall damage\n[Min: 0, Default: 3] [DECIMAL SUPPORTED]")
    public float freezeFallIceDamage = 3;

    @SendToClient
    @SerialEntry(comment = "The duration of the slowdown effect\n[Min: 0, Default: ??]")
    public int freezeSlowdownTime = 50;

    @SendToClient
    @SerialEntry(comment = "The maximum player speed slowdown\n[Min: 0, Default: ??]")
    public float freezeSlowdown = 0.2f;

    @SendToClient
    @SerialEntry(comment = "The duration of the ice\n[Min: 0, Default: ??]")
    public int freezeIceTime = 100;
}
