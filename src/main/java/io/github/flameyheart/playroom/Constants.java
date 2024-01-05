package io.github.flameyheart.playroom;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.sound.SoundCategory;

public class Constants {
    public static final byte PROTOCOL_VERSION = 2;
    public static final SoundCategory PLAYROOM_SOUND_CATEGORY = ClassTinkerers.getEnum(SoundCategory.class, "PLAYROOM");
}
