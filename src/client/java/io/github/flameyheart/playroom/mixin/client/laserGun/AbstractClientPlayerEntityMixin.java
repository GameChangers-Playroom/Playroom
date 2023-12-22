package io.github.flameyheart.playroom.mixin.client.laserGun;

import com.mojang.authlib.GameProfile;
import io.github.flameyheart.playroom.PlayroomClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin extends PlayerEntity {
    public AbstractClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "getFovMultiplier", at = @At("TAIL"), cancellable = true)
    public void zoomLevel(CallbackInfoReturnable<Float> ci) {
        if (PlayroomClient.isAiming(this.getMainHandStack())) {
            ci.setReturnValue(0.125f);
        }
    }
}