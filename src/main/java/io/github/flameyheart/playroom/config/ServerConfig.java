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
    //endregion

    //region LASER GUN
    @SendToClient
    @SerialEntry(comment = "How much amo the rapid fire mode has\n[Min: 0, Max: 127, Default: 5]")
    public byte laserRapidFireAmo = 12;

    @SendToClient
    @SerialEntry(comment = "The laser reload time when it hits a player\n[Min: 0, Max: 32767, Default: 3600]")
    public short laserHitReloadTime = 3600;

    @SendToClient
    @SerialEntry(comment = "The laser reload time when it doesn't hit a player\n[Min: 0, Max: 32767, Default: 1200]")
    public short laserMissReloadTime = 1200;

    @SendToClient
    @SerialEntry(comment = "The laser reach\n[Min: 0, Max: 127, Default: 12]")
    public byte laserReach = 12;

    @SendToClient
    @SerialEntry(comment = "The cooldown applied to when the player swaps the gun mode\n[Min: 0, Max: 32767, Default: 10]")
    public short laserSwapModeCooldown = 10;

    @SerialEntry(comment = "The speed of the ranged mode ice shot\n[Min: 0.1, Default: 3] [DECIMAL SUPPORTED]")
    public float laserRangedBulletSpeed = 3;

    @SerialEntry(comment = "The divergence of the ranged mode ice shot\n[Min: 0, Default: 0] [DECIMAL SUPPORTED]")
    public float laserRangedDivergence = 0;

    @SerialEntry(comment = "The maximum life time of a shot, this is to avoid eternal items on the sky\n[Min: 0, Default: 0] [DECIMAL SUPPORTED]")
    public float laserRangedLifeTime = 300;

    @SerialEntry(comment = "The speed of the rapid fire mode ice shot\n[Min: 0.1, Default: 3] [DECIMAL SUPPORTED]")
    public float laserRapidBulletSpeed = 3;

    @SerialEntry(comment = "The divergence of the rapid fire mode ice shot\n[Min: 0, Default: 3] [DECIMAL SUPPORTED]")
    public float laserRapidDivergence = 0;
    //endregion
}
