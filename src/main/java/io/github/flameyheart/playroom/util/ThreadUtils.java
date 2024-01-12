package io.github.flameyheart.playroom.util;

import org.jetbrains.annotations.Nullable;

public class ThreadUtils {
    public static void tryStart(@Nullable Thread thread) {
        if (thread == null) return;
        thread.start();
    }

    public static void interrupt(@Nullable Thread thread) {
        if (thread == null) return;
        thread.interrupt();
    }

    public static boolean isAlive(@Nullable Thread thread) {
        return thread != null && thread.isAlive();
    }
}
