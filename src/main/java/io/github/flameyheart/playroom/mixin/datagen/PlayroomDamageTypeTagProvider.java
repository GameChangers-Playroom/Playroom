package io.github.flameyheart.playroom.mixin.datagen;

import io.github.flameyheart.playroom.registry.Damage;
import net.minecraft.data.DataOutput;
import net.minecraft.data.server.tag.TagProvider;
import net.minecraft.data.server.tag.vanilla.VanillaDamageTypeTagProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(VanillaDamageTypeTagProvider.class)
public abstract class PlayroomDamageTypeTagProvider extends TagProvider<DamageType> {
    protected PlayroomDamageTypeTagProvider(DataOutput output, RegistryKey<? extends Registry<DamageType>> registryRef, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture) {
        super(output, registryRef, registryLookupFuture);
    }

    @Inject(method = "configure", at = @At("TAIL"))
    private void addPlayroomDamageTypes(CallbackInfo ci) {
        this.getOrCreateTagBuilder(DamageTypeTags.NO_IMPACT).add(Damage.LASER_SHOT);
        this.getOrCreateTagBuilder(DamageTypeTags.AVOIDS_GUARDIAN_THORNS).add(Damage.LASER_SHOT);
        this.getOrCreateTagBuilder(DamageTypeTags.IS_PROJECTILE).add(Damage.LASER_SHOT);
    }
}
