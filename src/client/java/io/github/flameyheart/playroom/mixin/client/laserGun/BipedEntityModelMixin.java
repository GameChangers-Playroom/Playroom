package io.github.flameyheart.playroom.mixin.client.laserGun;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.github.flameyheart.playroom.PlayroomClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin<T extends LivingEntity> {
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart head;

    @Shadow public BipedEntityModel.ArmPose rightArmPose;
    @Shadow public BipedEntityModel.ArmPose leftArmPose;

    @WrapWithCondition(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/CrossbowPosing;swingArm(Lnet/minecraft/client/model/ModelPart;FF)V"))
    private boolean disableArmAnimation(ModelPart arm, float animationProgress, float sigma, T livingEntity) {
        return !((this.rightArmPose == PlayroomClient.LASER_GUN_POSE || this.leftArmPose == PlayroomClient.LASER_GUN_POSE) && livingEntity.getItemUseTime() > 0);
    }

    @Inject(method = "positionRightArm", at = @At("HEAD"), cancellable = true)
    private void laserGunRightArm(T entity, CallbackInfo ci) {
        float pi2 = (float) (Math.PI / 2);
        if (this.rightArmPose == PlayroomClient.LASER_GUN_POSE) {
            this.rightArm.pitch = this.head.pitch - pi2;
            this.leftArm.pitch = Math.min(-0.2f, this.head.pitch - pi2 + 0.1f);
            this.rightArm.yaw = this.head.yaw;
            this.leftArm.roll = this.head.pitch / 2;
            this.leftArm.yaw = this.head.yaw + pi2 / 2;
            ci.cancel();
        }
    }

    @Inject(method = "positionLeftArm", at = @At("HEAD"), cancellable = true)
    private void laserGunLeftArm(T entity, CallbackInfo ci) {
        if (this.leftArmPose == PlayroomClient.LASER_GUN_POSE) {
            this.leftArm.pitch = this.head.pitch - 1.5707964f;
            this.rightArm.pitch = this.head.pitch - 1.5707964f;
            this.leftArm.yaw = this.head.yaw + 0.1f;
            this.rightArm.yaw = this.head.yaw - 0.5f;
            ci.cancel();
        }
    }
}
