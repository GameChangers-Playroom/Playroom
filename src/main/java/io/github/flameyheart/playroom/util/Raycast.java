package io.github.flameyheart.playroom.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class Raycast {
    public static HitResult raycast(World world, Entity entity, double maxDistance, boolean includeFluids, boolean includeEntity) {
        Vec3d start = entity.getCameraPosVec(0);
        Vec3d direction = entity.getRotationVec(0);
        Vec3d end = start.add(direction.multiply(maxDistance));
        HitResult result = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, entity));
        double reach = maxDistance;
        reach *= reach;

        if (result != null) {
            reach = result.getPos().squaredDistanceTo(start);
        }

        if (includeEntity) {
            end = start.add(direction.x * maxDistance, direction.y * maxDistance, direction.z * maxDistance);
            Box box = entity.getBoundingBox().stretch(direction.multiply(maxDistance)).expand(1.0, 1.0, 1.0);
            EntityHitResult entityHitResult = ProjectileUtil.raycast(entity, start, end, box, entity2 -> !entity2.isSpectator() && entity2.canHit(), reach);
            if (entityHitResult != null) {
                Vec3d vec3d4 = entityHitResult.getPos();
                double distance = start.squaredDistanceTo(vec3d4);
                if (distance < reach || result == null) {
                    result = entityHitResult;
                }
            }
        }
        return result;
    }

    public static HitResult raycastEntity(Entity entity, double maxDistance) {
        Vec3d start = entity.getCameraPosVec(0);
        Vec3d direction = entity.getRotationVec(0);
        Vec3d end = start.add(direction.x * maxDistance, direction.y * maxDistance, direction.z * maxDistance);
        double reach = maxDistance;
        reach *= reach;

        Box box = entity.getBoundingBox().stretch(direction.multiply(maxDistance)).expand(1.0, 1.0, 1.0);
        EntityHitResult result = ProjectileUtil.raycast(entity, start, end, box, entity2 -> !entity2.isSpectator() && entity2.canHit(), reach);
        if (result != null) {
            Vec3d vec3d4 = result.getPos();
            double distance = start.squaredDistanceTo(vec3d4);
            if (distance < reach) {
                return result;
            }
        }
        return new HitResult(end) {
            @Override
            public Type getType() {
                return Type.MISS;
            }
        };
    }
}
