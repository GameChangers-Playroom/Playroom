package io.github.flameyheart.playroom.entity;

import io.github.flameyheart.playroom.Constants;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.duck.FreezableEntity;
import io.github.flameyheart.playroom.mixin.FluidBlockAccessor;
import io.github.flameyheart.playroom.registry.Damage;
import io.github.flameyheart.playroom.registry.Entities;
import io.github.flameyheart.playroom.registry.Sounds;
import io.github.flameyheart.playroom.registry.Tags;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class LaserProjectileEntity extends PersistentProjectileEntity {
    private static final TrackedData<Boolean> RAPID_FIRE = DataTracker.registerData(LaserProjectileEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public LaserProjectileEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
        setNoGravity(true);
    }

    public LaserProjectileEntity(EntityType<? extends PersistentProjectileEntity> type, double x, double y, double z, World world) {
        super(type, x, y, z, world);
        setNoGravity(true);
    }

    public LaserProjectileEntity(World world, LivingEntity owner, boolean rapidFire) {
        super(Entities.LASER_SHOT, owner, world);
        setRapidFire(rapidFire);
        setNoGravity(true);
    }

    public static @NotNull LaserProjectileEntity create(World world, LivingEntity owner, boolean isRapidFire) {
        return new LaserProjectileEntity(world, owner, isRapidFire);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(RAPID_FIRE, false);
    }

    @Override
    protected void onEntityHit(@NotNull EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        if (entity == null) return;
        if (entity.getWorld().isClient()) return;
        this.discard();
        if (getOwner() == null || entity.getUuid().equals(getOwner().getUuid()) || !(entity instanceof FreezableEntity freezableEntity) || !(entity instanceof LivingEntity living)) {
            return;
        }
        if (!entity.getType().isIn(Tags.IMMUNE_TO_FREEZE)) {
            if (getOwner() instanceof LivingEntity) {
                ((LivingEntity) getOwner()).onAttacking(entity);
            }
            if (entity.damage(Damage.laserShot(this.getWorld(), this, getOwner()), (float) getDamage())) {
                this.onHit(living);
                playSound(entity.getWorld(), entity, getHitSound());
                if (isRapidFire()) {
                    if (freezableEntity.playroom$isFrozen()) {
                        freezableEntity.playroom$addFreezeTime(ServerConfig.instance().laserRapidFreezeAmount / 2);
                    } else {
                        freezableEntity.playroom$addSlowdownTime(ServerConfig.instance().laserRapidFreezeAmount);
                    }
                } else {
                    freezableEntity.playroom$freeze();
                }
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        this.discard();
    }

    @Override
    public boolean updateMovementInFluid(TagKey<Fluid> tag, double speed) {
        if (ServerConfig.instance().laserFreezeWater) {
            if (getWorld().getBlockState(getBlockPos()).getBlock() instanceof FluidBlock fluid) {
                if (((FluidBlockAccessor) fluid).getFluid() == Fluids.WATER) {
                    getWorld().setBlockState(getBlockPos(), Blocks.FROSTED_ICE.getDefaultState());
                    this.discard();
                }
            }
        }
        return super.updateMovementInFluid(tag, speed);
    }

    public void setRapidFire(boolean rapidFire) {
        this.dataTracker.set(RAPID_FIRE, rapidFire);
    }

    public boolean isRapidFire() {
        return this.dataTracker.get(RAPID_FIRE);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("rapidFire", isRapidFire());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("rapidFire")) {
            setRapidFire(nbt.getBoolean("rapidFire"));
        }
    }

    @Override
    protected ItemStack asItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected SoundEvent getHitSound() {
        return isRapidFire() ? Sounds.FREEZE : Sounds.HIT_FREEZE;
    }

    @Override
    public double getDamage() {
        return isRapidFire() ? ServerConfig.instance().laserRapidDamage : ServerConfig.instance().laserRangeDamage;
    }

    @Override
    protected float getDragInWater() {
        return 1;
    }

    public void playSound(World world, Entity target, SoundEvent sound) {
        if (!world.isClient) {
            world.playSound(null, target.getX(), target.getY(), target.getZ(), sound, Constants.PLAYROOM_SOUND_CATEGORY, 0.5F, 1.0F);
        }
    }
}
