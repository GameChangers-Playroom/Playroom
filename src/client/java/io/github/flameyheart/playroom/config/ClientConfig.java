package io.github.flameyheart.playroom.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;
import io.github.flameyheart.playroom.Playroom;

public class ClientConfig {
    public static final ConfigClassHandler<ClientConfig> INSTANCE = ConfigClassHandler.createBuilder(ClientConfig.class)
      .id(Playroom.id("config"))
      .serializer(config -> GsonConfigSerializerBuilder.create(config)
        .setPath(YACLPlatform.getConfigDir().resolve("playroom/client.json5"))
        .setJson5(true)
        .build()
      ).build();

    public static ClientConfig instance() {
        return INSTANCE.instance();
    }

    public static ClientConfig defaults() {
        return INSTANCE.defaults();
    }

    @SerialEntry(comment = "Disables some animations to reduce motion sickness\n[Default: false]")
    public boolean reducedMotion = false;
    @SerialEntry(comment = "Enables debug info\n[Default: false]")
    public boolean debugInfo = false;
}
