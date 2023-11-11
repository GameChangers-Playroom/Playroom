package io.github.flameyheart.playroom.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;
import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.config.annotations.SendToClient;

public class ServerConfig {
    public static ConfigClassHandler<ServerConfig> INSTANCE = ConfigClassHandler.createBuilder(ServerConfig.class)
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
    @SerialEntry(comment = "The laser reload time when it hits a player\n[Min: 0, Max: 32767, Default: 3600]")
    public short laserHitReloadTime = 3600;

    @SendToClient
    @SerialEntry(comment = "The laser reload time when it doesn't hit a player\n[Min: 0, Max: 32767, Default: 1200]")
    public short laserMissReloadTime = 1200;

    @SendToClient
    @SerialEntry(comment = "The laser reach\n[Min: 0, Max: 127, Default: 12]")
    public byte laserReach = 12;

    @SendToClient
    @SerialEntry(comment = "The speedup of when you use the quick shot mode\n[Min: 0, Default: 2]")
    public float quickShotSpeedup = 2;

    @SendToClient
    @SerialEntry(comment = "The cooldown applied to when the player swaps the gun mode\n[Min: 0, Default: 10]")
    public short swapModeCooldown = 10;
    //endregion
}
