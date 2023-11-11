package io.github.flameyheart.playroom.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;
import io.github.flameyheart.playroom.Playroom;

public class ClientConfig {
    public static ConfigClassHandler<ClientConfig> INSTANCE = ConfigClassHandler.createBuilder(ClientConfig.class)
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

    public @SerialEntry float value1 = 0;
    public @SerialEntry float value2 = 0;
    public @SerialEntry float value3 = 0;
    public @SerialEntry float value4 = 0;
    public @SerialEntry float value5 = 0;
    public @SerialEntry float value6 = 0;
    public @SerialEntry float value7 = 0;
    public @SerialEntry float value8 = 0;
    public @SerialEntry float value9 = 0;
}
