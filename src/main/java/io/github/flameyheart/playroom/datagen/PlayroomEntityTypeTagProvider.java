package io.github.flameyheart.playroom.datagen;

import io.github.flameyheart.playroom.registry.Tags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class PlayroomEntityTypeTagProvider extends FabricTagProvider.EntityTypeTagProvider {
    public PlayroomEntityTypeTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        this.getOrCreateTagBuilder(Tags.IMMUNE_TO_FREEZE).add(EntityType.ENDER_DRAGON, EntityType.WARDEN, EntityType.WITHER, EntityType.STRAY);;
    }
}
