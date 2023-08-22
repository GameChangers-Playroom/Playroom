package io.github.flameyheart.playroom.compat;

import net.fabricmc.loader.api.FabricLoader;

public class ModOptional {
    public static boolean isPresent(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static void ifPresent(String modId, Runnable runnable) {
        if (isPresent(modId)) {
            runnable.run();
        }
    }
}
