package io.github.flameyheart.playroom.registry;

import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class Particles implements AutoRegistryContainer<ParticleType<?>> {

    public static final DefaultParticleType TEST_PARTICLE = FabricParticleTypes.simple();

    @Override
    public Registry<ParticleType<?>> getRegistry() {
        return Registries.PARTICLE_TYPE;
    }

    @Override
    public Class<ParticleType<?>> getTargetFieldType() {
        return AutoRegistryContainer.conform(ParticleType.class);
    }
}
