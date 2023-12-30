package io.github.flameyheart.playroom.registry;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.entity.LaserProjectileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class Damage {
    public static final RegistryKey<DamageType> LASER_SHOT = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Playroom.id("laser_shot"));
    public static DamageSource laserShot(World world, LaserProjectileEntity source, @Nullable Entity attacker) {
        return new DamageSource(getEntry(world, LASER_SHOT), source, attacker);
    }

    public static DamageSource of(World world, RegistryKey<DamageType> key) {
        return new DamageSource(getEntry(world, key));
    }

    private static RegistryEntry<DamageType> getEntry(World world, RegistryKey<DamageType> key) {
        return world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key);
    }
}
