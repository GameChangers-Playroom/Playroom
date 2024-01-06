package io.github.flameyheart.playroom.datagen;

import io.github.flameyheart.playroom.datagen.provider.DamageTypeTagProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class PlayroomDamageTypeTagProvider extends DamageTypeTagProvider {
    public PlayroomDamageTypeTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        //Idk why, but datagen hates damage type
        //this.getOrCreateTagBuilder(Tags.NO_HURT_SOUND).add(Damage.LASER_SHOT);
        //this.getOrCreateTagBuilder(Tags.NO_TILT);
    }
}
