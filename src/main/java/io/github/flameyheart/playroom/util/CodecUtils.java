package io.github.flameyheart.playroom.util;

import com.mojang.serialization.Codec;

public class CodecUtils {

    public static <T extends Enum<T>> Codec<T> enumCodec(Class<T> enumClass) {
        return Codec.STRING.xmap(str -> Enum.valueOf(enumClass, str), Enum::name);
    }
}
