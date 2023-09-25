package io.github.flameyheart.playroom.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;
import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.config.annotations.Category;
import io.github.flameyheart.playroom.config.annotations.SendToClient;

public class ServerConfig {
    public static ConfigClassHandler<ServerConfig> INSTANCE = ConfigClassHandler.createBuilder(ServerConfig.class)
        .id(Playroom.id("config"))
        .serializer(config -> GsonConfigSerializerBuilder.create(config)
            .setPath(YACLPlatform.getConfigDir().resolve("playroom/server.json5"))
            .setJson5(true)
            .build()
        ).build();

    public static ServerConfig instance() {
        return INSTANCE.instance();
    }

    @Category("networking")
    @SerialEntry(comment = "Whether or not to allow vanilla players to join the server")
    public boolean allowVanillaPlayers = false;

    @Category("networking")
    @SerialEntry(comment = "Will kick players with mismatching protocol versions")
    public boolean requireMatchingProtocol = false;

    @SendToClient
    @Category("laser")
    @SerialEntry(comment = "The laser reload time when it hits a player")
    public short laserHitReloadTime = 3600;

    @SendToClient
    @Category("laser")
    @SerialEntry(comment = "The laser reload time when it doesn't hit a player")
    public short laserMissReloadTime = 1200;

    @SendToClient
    @Category("laser")
    @SerialEntry(comment = "The speedup of when you use the quick shot mode")
    public float quickShotSpeedup = 2;
}
