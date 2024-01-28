package io.github.flameyheart.playroom.registry;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.entity.LaserProjectileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class Damage {
    public static final RegistryKey<DamageType> LASER_SHOT = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Playroom.id("laser_shot"));
    public static DamageSource laserShot(World world, LaserProjectileEntity source, @Nullable Entity attacker) {
        return new LaserShotDamageSource(getEntry(world, LASER_SHOT), source, attacker);
    }

    public static DamageSource of(World world, RegistryKey<DamageType> key) {
        return new DamageSource(getEntry(world, key));
    }

    private static RegistryEntry<DamageType> getEntry(World world, RegistryKey<DamageType> key) {
        return world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key);
    }

    public static class LaserShotDamageSource extends DamageSource {
        public LaserShotDamageSource(RegistryEntry<DamageType> type, @Nullable Entity source, @Nullable Entity attacker) {
            super(type, source, attacker);
        }

        @Override
        public Text getDeathMessage(LivingEntity killed) {
            String string = getRandomDeathMessage(killed.getRandom());
            if (this.getAttacker() != null || this.getSource() != null) {
                ItemStack itemStack;
                Text text = this.getAttacker() == null ? this.getSource().getDisplayName() : this.getAttacker().getDisplayName();
                Entity entity = this.getAttacker();
                if (entity instanceof LivingEntity livingEntity) {
                    itemStack = livingEntity.getMainHandStack();
                } else {
                    itemStack = ItemStack.EMPTY;
                }
                if (!itemStack.isEmpty() && itemStack.hasCustomName()) {
                    return Text.translatable(string + ".item", killed.getDisplayName(), text, itemStack.toHoverableText());
                }
                return Text.translatable(string, killed.getDisplayName(), text);
            }
            LivingEntity livingEntity2 = killed.getPrimeAdversary();
            String string2 = string + ".player";
            if (livingEntity2 != null) {
                return Text.translatable(string2, killed.getDisplayName(), livingEntity2.getDisplayName());
            }
            return Text.translatable(string + ".self", killed.getDisplayName());
        }

        private String getRandomDeathMessage(Random random) {
            int i = random.nextBetween(0, 2);
            return "death.attack.%s.%d".formatted(this.getType().msgId(), i);
        }
    }
}
