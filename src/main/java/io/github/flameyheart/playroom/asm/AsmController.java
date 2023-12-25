package io.github.flameyheart.playroom.asm;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class AsmController implements Runnable {
    @Override
    public void run() {
        new PlayroomAsm().run();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            try {
                Class<?> asmClass = Class.forName("io.github.flameyheart.playroom.asm.PlayroomClientAsm");
                Runnable asm = (Runnable) asmClass.getConstructor().newInstance();
                asm.run();
            } catch (ClassCastException | ReflectiveOperationException ignored) {}
        }
    }
}
