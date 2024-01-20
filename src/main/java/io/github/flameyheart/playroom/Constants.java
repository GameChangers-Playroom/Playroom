package io.github.flameyheart.playroom;

import com.chocohead.mm.api.ClassTinkerers;
import io.github.flameyheart.playroom.util.MapBuilder;
import net.minecraft.sound.SoundCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Constants {
    public static final byte PROTOCOL_VERSION = 2;
    public static final SoundCategory PLAYROOM_SOUND_CATEGORY = ClassTinkerers.getEnum(SoundCategory.class, "PLAYROOM");
    public static final Map<String, String> ALTERNATIVE_NAMES = new MapBuilder.StringMap()
      // Key: Alternative name, Value: MC username
      .put("sparklez", "CaptainSparklez")
      .put("FloraKiiro", "Yazmin_")
      .put("UrsenTV", "Ursen")
      .put("Nukeri", "Nukerii")
      .put("OGQndres", "Qndres")
      .put("JustDashYT", "JustDash")
      .put("lucy", "lucytgirl_")
      .put("penguinkidLIVE", "penguinkid")
      .put("floorb", "floorb_")
      .immutable();
    public static final List<String> POSSIBLE_NAMES;

    static {
        ArrayList<String> possibleNames = new ArrayList<>();
        ALTERNATIVE_NAMES.forEach((key, value) -> {
            if (!possibleNames.contains(key)) {
                possibleNames.add(key);
            }
            if (!possibleNames.contains(value)) {
                possibleNames.add(value);
            }
        });
        POSSIBLE_NAMES = List.copyOf(possibleNames);
    }
}
