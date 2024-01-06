package io.github.flameyheart.playroom.datagen;

import io.github.flameyheart.playroom.registry.Tags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class PlayroomItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public PlayroomItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(Tags.CRUNCHY_CRYSTALS)
          .add(
            Items.DIAMOND,
            Items.EMERALD,
            Items.QUARTZ,
            Items.SMALL_AMETHYST_BUD,
            Items.MEDIUM_AMETHYST_BUD,
            Items.LARGE_AMETHYST_BUD,
            Items.AMETHYST_CLUSTER
          );
    }
}
