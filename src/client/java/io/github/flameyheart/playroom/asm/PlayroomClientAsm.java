package io.github.flameyheart.playroom.asm;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class PlayroomClientAsm implements Runnable {
    @Override
    public void run() {
        MappingResolver remapper = FabricLoader.getInstance().getMappingResolver();

        String armPoseClass = remapper.mapClassName("intermediary", "net.minecraft.class_572$class_573");
        ClassTinkerers.enumBuilder(armPoseClass, boolean.class)
          .addEnum("LASER_GUN", true)
          .build();
    }
}
