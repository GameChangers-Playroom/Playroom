package io.github.flameyheart.playroom.asm;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class PlayroomAsm implements Runnable {
    @Override
    public void run() {
        MappingResolver remapper = FabricLoader.getInstance().getMappingResolver();

        String soundCategories = remapper.mapClassName("intermediary", "net.minecraft.class_3419");
        ClassTinkerers.enumBuilder(soundCategories, String.class)
          .addEnum("PLAYROOM", "playroom")
          .build();
    }
}
