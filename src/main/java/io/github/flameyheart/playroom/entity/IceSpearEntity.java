package io.github.flameyheart.playroom.entity;

import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.registry.Entities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class IceSpearEntity extends ProjectileEntity {
    private short life;

    public IceSpearEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    private IceSpearEntity(World world, LivingEntity owner) {
        super(Entities.ICE_SPEAR, world);
        this.setPosition(owner.getX(), owner.getEyeY() - (double)0.1f, owner.getZ());
        this.setOwner(owner);
    }

    public static IceSpearEntity create(World world, LivingEntity owner) {
        return new IceSpearEntity(world, owner);
    }

    @Override
    protected void initDataTracker() {/**/}

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        if (entity instanceof ExpandedEntityData entityData && getOwner() != null) {
            entityData.playroom$freeze();
        }
        this.discard();
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient) {
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (life++ >= ServerConfig.instance().laserRangedLifeTime && !this.getWorld().isClient) {
            this.discard();
            return;
        }
        Vec3d velocity = this.getVelocity();
        Vec3d pos = this.getPos();
        setPosition(pos.add(velocity));
        this.checkBlockCollision();
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putShort("life", this.life);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.life = nbt.getShort("life");
    }
}
