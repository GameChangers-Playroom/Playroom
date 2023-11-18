package io.github.flameyheart.playroom.entity;

import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.registry.Entities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
        if (entity instanceof ExpandedEntityData entityData && getOwner() != null) {
            if (isRapidFire()) {
                entityData.playroom$addGunFreezeTicks(100);
            } else {
                entityData.playroom$freeze();
            }
        }
        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        this.discard();
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
}
