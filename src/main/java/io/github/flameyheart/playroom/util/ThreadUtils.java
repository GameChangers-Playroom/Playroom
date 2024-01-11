package io.github.flameyheart.playroom.util;

import org.jetbrains.annotations.Nullable;

public class ThreadUtils {
    public static void tryStart(@Nullable Thread thread) {
        if (thread == null) return;
        thread.start();
    }
}
