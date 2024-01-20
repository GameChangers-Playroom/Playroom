package io.github.flameyheart.playroom.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;
import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.dontation.DonationLocation;
import io.github.flameyheart.playroom.zoom.TransitionType;

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


    @SerialEntry(comment = "Where donations appear on screen\n[Default: CHAT]")
    public DonationLocation dontationLocation = DonationLocation.CHAT;

    @SerialEntry(comment = "The duration of the donation display in ticks\n[Min: 0, Default: 100]")
    public int donationExpiryTime = 100;

    @SerialEntry(comment = "Disables some animations to reduce motion sickness\n[Default: false]")
    public boolean reducedMotion = false;

    @SerialEntry(comment = "Enables debug info\n[Default: false]")
    public boolean debugInfo = false;

    @SerialEntry(comment = "The relative sensitivity of the camera while aiming\n[Min: 0, Default: 100]")
    public short laserAimCameraSmoothness = 100;

    @SerialEntry(comment = "The zoom in transition when aiming\n[Default: EASE_OUT_EXP]")
    public TransitionType laserAimZoomInTransition = TransitionType.EASE_OUT_EXP;

    @SerialEntry(comment = "The zoom out transition when aiming\n[Default: EASE_IN_EXP]")
    public TransitionType laserAimZoomOutTransition = TransitionType.EASE_OUT_EXP;

    @SerialEntry(comment = "The zoom in duration when aiming\n[Min: 0, Default: 0.3] [DECIMAL SUPPORTED]")
    public double laserAimZoomInTime = 0.3;

    @SerialEntry(comment = "The zoom out duration when aiming\n[Min: 0, Default: 0.2] [DECIMAL SUPPORTED]")
    public double laserAimZoomOutTime = 0.2;

    @SerialEntry(comment = "The duration of the zoom effect\n[Min: 0, Default: 0.3] [DECIMAL SUPPORTED]")
    public double freezeZoomDuration = 0.3;

    @SerialEntry(comment = "The target FOV for the zoom effect\n[Min: 0, Default: 0.1] [DECIMAL SUPPORTED]")
    public int freezeZoomTarget = 10;

    @SerialEntry(comment = "The zoom in transition when frozen\n[Default: EASE_OUT_EXP]")
    public TransitionType freezeZoomTransition = TransitionType.EASE_OUT_EXP;
}
