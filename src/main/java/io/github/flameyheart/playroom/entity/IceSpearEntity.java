package io.github.flameyheart.playroom.entity;

import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.duck.LivingEntityExtender;
import io.github.flameyheart.playroom.registry.Entities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class IceSpearEntity extends PersistentProjectileEntity {
    public IceSpearEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    private IceSpearEntity(World world, LivingEntity owner) {
        super(Entities.ICE_SPEAR, owner, world);
    }

    public IceSpearEntity(World world, double x, double y, double z) {
        super(Entities.ICE_SPEAR, x, y, z, world);
    }

    public static IceSpearEntity create(World world, LivingEntity owner) {
        return new IceSpearEntity(world, owner);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        if (entity instanceof ExpandedEntityData entityData && getOwner() != null) {
            entityData.playroom$freeze();
            if(entity instanceof LivingEntityExtender extender) {
                extender.playroom$setStuckSpearCount(extender.playroom$getStuckSpearCount() + 1);
            }
        }
        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        this.discard();
    }

    @Override
    protected ItemStack asItemStack() {
        return ItemStack.EMPTY;
    }
}
