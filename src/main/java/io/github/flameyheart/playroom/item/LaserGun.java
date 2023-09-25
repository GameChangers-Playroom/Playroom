package io.github.flameyheart.playroom.item;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.util.Raycast;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Vanishable;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LaserGun extends Item implements Vanishable {
    public LaserGun(Settings settings) {
        super(settings);
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return false;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        Vec3d start = user.getCameraPosVec(0).add(0, -0.4, 0);

        if (stack.getOrCreateNbt().getLong("cooldownExpires") > Playroom.getServer().getOverworld().getTime()) {
            return TypedActionResult.fail(stack);
        }

        HitResult raycast = Raycast.raycast(world, user, 12, false, true);

        int cooldownTime;
        if (raycast.getType() == HitResult.Type.ENTITY && ((EntityHitResult) raycast).getEntity() instanceof PlayerEntity target) {
            cooldownTime = ServerConfig.instance().laserHitReloadTime;

            target.setFrozenTicks(target.getMinFreezeDamageTicks() + 200);
        } else {
            cooldownTime = ServerConfig.instance().laserMissReloadTime;
        }

        stack.getOrCreateNbt().putLong("cooldownExpires", Playroom.getServer().getOverworld().getTime() + cooldownTime);

        if (world instanceof ServerWorld serverWorld) {
            double distance = Math.sqrt(raycast.getPos().squaredDistanceTo(start));

            Vec3d direction = user.getRotationVec(0);

            for (double i = 0; i < distance; i += 0.01 * distance) {
                Vec3d end = start.add(direction.x * i, direction.y * i, direction.z * i);
                spawnParticles(serverWorld, new DustParticleEffect(Vec3d.unpackRgb(0xFF0000).toVector3f(), 1.0f),
                    end.x, end.y, end.z, 1, 0, 0, 0, 1);

            }
            user.swingHand(hand, true);
        }

        return TypedActionResult.pass(stack);
    }

    protected  <T extends ParticleEffect> int spawnParticles(ServerWorld world, T particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
        ParticleS2CPacket particleS2CPacket = new ParticleS2CPacket(particle, true, x, y, z, (float)deltaX, (float)deltaY, (float)deltaZ, (float)speed, count);
        int i = 0;
        for (int j = 0; j < world.getPlayers().size(); ++j) {
            ServerPlayerEntity serverPlayerEntity = world.getPlayers().get(j);
            if (!world.sendToPlayerIfNearby(serverPlayerEntity, true, x, y, z, particleS2CPacket)) continue;
            ++i;
        }
        return i;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        long cooldown = stack.getOrCreateNbt().getLong("cooldown");
        long cooldownExpires = stack.getOrCreateNbt().getLong("cooldownExpires");
        long time = Playroom.getServer().getOverworld().getTime();
        long timeLeft = cooldownExpires - time;

        return Math.round(13.0F - timeLeft * 13.0F / (float) cooldown);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        long cooldown = stack.getOrCreateNbt().getLong("cooldown");
        long cooldownExpires = stack.getOrCreateNbt().getLong("cooldownExpires");
        long time = Playroom.getServer().getOverworld().getTime();
        long timeLeft = cooldownExpires - time;

        float f = Math.max(0f, ((float) cooldown - timeLeft) / (float) cooldown);

        return MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return stack.getOrCreateNbt().getLong("cooldownExpires") > Playroom.getServer().getOverworld().getTime();
    }
}
