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
      .put("sparklypants", "CaptainSparklez")
      .put("captain", "CaptainSparklez")
      .put("jordan", "CaptainSparklez")
      .put("florakiiro", "Yazmin_")
      .put("ursentv", "Ursen")
      .put("nukeri", "Nukerii")
      .put("ogqndres", "Qndres")
      .put("justdashyt", "JustDash")
      .put("lucy", "lucytgirl_")
      .put("lucytgirl", "lucytgirl_")
      .put("penguinkid", "penguinkidLIVE")
      .put("floorb", "floorb_")
      .put("fluffle", "iamfluffle")
      .put("matias", "OhMatias")
      .put("thespryte", "the_spryte")
      .put("Chai", "ChaitheChuu")
      .put("debbie", "debbiemorales")
      .put("sophie", "sophietexas")
      .put("Kyle", "KyleEff")
      .put("Piggles", "ItzIgglesPiggles")
      .put("Sanz", "AwesomeSanz")
      .put("honk", "honkshroom")
      .put("shroom", "honkshroom")
      .put("prime", "PrimeGreen")
      .put("green", "PrimeGreen")
      .put("zodi", "zodiacsiren")
      .put("zodiac", "zodiacsiren")
      .put("rin", "rinthehecker")
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
