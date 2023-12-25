package io.github.flameyheart.playroom.mixin.client.freeze.render;

import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.render.entity.PlayerModelPositions;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin<T extends LivingEntity> {
    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart hat;
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightLeg;
    @Shadow @Final public ModelPart leftLeg;

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"), cancellable = true)
    private void stopAnimations(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (livingEntity instanceof PlayerEntity player) {
            ExpandedEntityData entity = (ExpandedEntityData) livingEntity;
            if (entity.playroom$showIce()) {
                PlayerModelPositions positions = PlayroomClient.frozenModel.get(player);
                if (positions != null) {
                    List.of(this.head, this.hat, this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg).forEach(modelPart -> {
                        modelPart.pivotX = positions.head().pivotX();
                        modelPart.pivotY = positions.head().pivotY();
                        modelPart.pivotZ = positions.head().pivotZ();
                        modelPart.roll = positions.head().roll();
                        modelPart.yaw = positions.head().yaw();
                        modelPart.pitch = positions.head().pitch();
                    });
                    ci.cancel();
                } else {
                    PlayerModelPositions.ModelPosition head = new PlayerModelPositions.ModelPosition(this.head.pivotX, this.head.pivotY, this.head.pivotZ, this.head.roll, this.head.yaw, this.head.pitch);
                    PlayerModelPositions.ModelPosition hat = new PlayerModelPositions.ModelPosition(this.hat.pivotX, this.hat.pivotY, this.hat.pivotZ, this.hat.roll, this.hat.yaw, this.hat.pitch);
                    PlayerModelPositions.ModelPosition body = new PlayerModelPositions.ModelPosition(this.body.pivotX, this.body.pivotY, this.body.pivotZ, this.body.roll, this.body.yaw, this.body.pitch);
                    PlayerModelPositions.ModelPosition rightArm = new PlayerModelPositions.ModelPosition(this.rightArm.pivotX, this.rightArm.pivotY, this.rightArm.pivotZ, this.rightArm.roll, this.rightArm.yaw, this.rightArm.pitch);
                    PlayerModelPositions.ModelPosition leftArm = new PlayerModelPositions.ModelPosition(this.leftArm.pivotX, this.leftArm.pivotY, this.leftArm.pivotZ, this.leftArm.roll, this.leftArm.yaw, this.leftArm.pitch);
                    PlayerModelPositions.ModelPosition rightLeg = new PlayerModelPositions.ModelPosition(this.rightLeg.pivotX, this.rightLeg.pivotY, this.rightLeg.pivotZ, this.rightLeg.roll, this.rightLeg.yaw, this.rightLeg.pitch);
                    PlayerModelPositions.ModelPosition leftLeg = new PlayerModelPositions.ModelPosition(this.leftLeg.pivotX, this.leftLeg.pivotY, this.leftLeg.pivotZ, this.leftLeg.roll, this.leftLeg.yaw, this.leftLeg.pitch);
                    PlayroomClient.frozenModel.put(player, new PlayerModelPositions(head, hat, body, rightArm, leftArm, rightLeg, leftLeg));
                }
            }
        }
    }
}
