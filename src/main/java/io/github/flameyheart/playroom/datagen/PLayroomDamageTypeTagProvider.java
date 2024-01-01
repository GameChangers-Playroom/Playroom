package io.github.flameyheart.playroom.datagen;

import io.github.flameyheart.playroom.registry.Damage;
import io.github.flameyheart.playroom.registry.Tags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.data.server.tag.TagProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.DamageTypeTags;

import java.util.concurrent.CompletableFuture;

public class PLayroomDamageTypeTagProvider extends TagProvider<DamageType> {
    public PLayroomDamageTypeTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.DAMAGE_TYPE, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        this.getOrCreateTagBuilder(DamageTypeTags.NO_IMPACT).add(Damage.LASER_SHOT);
        this.getOrCreateTagBuilder(DamageTypeTags.AVOIDS_GUARDIAN_THORNS).add(Damage.LASER_SHOT);
        this.getOrCreateTagBuilder(DamageTypeTags.IS_PROJECTILE).add(Damage.LASER_SHOT);
    }
}
