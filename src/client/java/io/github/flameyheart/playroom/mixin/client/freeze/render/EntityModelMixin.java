package io.github.flameyheart.playroom.mixin.client.freeze.render;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.render.entity.ModelPosition;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = {
  AbstractZombieModel.class, AllayEntityModel.class, ArmorStandArmorEntityModel.class, ArmorStandEntityModel.class,
  AxolotlEntityModel.class, BatEntityModel.class, BeeEntityModel.class, BipedEntityModel.class, BlazeEntityModel.class,
  CamelEntityModel.class, CatEntityModel.class, ChickenEntityModel.class, CodEntityModel.class, CreeperEntityModel.class,
  DolphinEntityModel.class, DonkeyEntityModel.class, DrownedEntityModel.class, EndermanEntityModel.class, EndermiteEntityModel.class,
  FoxEntityModel.class, FrogEntityModel.class, GhastEntityModel.class, GoatEntityModel.class, GuardianEntityModel.class,
  HoglinEntityModel.class, HorseEntityModel.class, IllagerEntityModel.class, IronGolemEntityModel.class, LargePufferfishEntityModel.class,
  LargeTropicalFishEntityModel.class, LlamaEntityModel.class, MagmaCubeEntityModel.class, MediumPufferfishEntityModel.class, OcelotEntityModel.class,
  PandaEntityModel.class, ParrotEntityModel.class, PhantomEntityModel.class, PiglinEntityModel.class, PlayerEntityModel.class,
  PolarBearEntityModel.class, QuadrupedEntityModel.class, RabbitEntityModel.class, RavagerEntityModel.class, SalmonEntityModel.class,
  SheepEntityModel.class, ShulkerEntityModel.class, SilverfishEntityModel.class, SkeletonEntityModel.class, SlimeEntityModel.class,
  SmallPufferfishEntityModel.class, SmallTropicalFishEntityModel.class, SnifferEntityModel.class, SnowGolemEntityModel.class, SpiderEntityModel.class,
  SquidEntityModel.class, StriderEntityModel.class, TadpoleEntityModel.class, VexEntityModel.class, VillagerResemblingModel.class,
  WardenEntityModel.class, WitchEntityModel.class, WitherEntityModel.class, WolfEntityModel.class
}, remap = false)
public class EntityModelMixin {
    @Unique private ModelPart playroom$root;

    @Inject(method = "<init>(Lnet/minecraft/client/model/ModelPart;)V", at = @At("TAIL"), require = 0)
    private void storeRoot0(CallbackInfo ci, @Local(argsOnly = true) ModelPart root) {
        playroom$root = root;
    }


    @Inject(method = {
      "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
      "method_2819(Lnet/minecraft/class_1309;FFFFF)V"
    }, at = @At("TAIL"))
    private void stopAnimations(CallbackInfo ci, @Local(argsOnly = true) LivingEntity livingEntity) {
        ExpandedEntityData entity = (ExpandedEntityData) livingEntity;
        if (entity.playroom$showIce()) {
            Map<String, ModelPart> children = ((ModelPartAccessor) playroom$root).getChildren();
            Map<String, ModelPosition> positions = PlayroomClient.FROZEN_MODEL.computeIfAbsent(livingEntity, v -> new HashMap<>());
            if (!positions.isEmpty()) {
                positions.forEach((name, position) -> {
                    ModelPart modelPart = playroom$root.getChild(name);
                    modelPart.pivotX = position.pivotX();
                    modelPart.pivotY = position.pivotY();
                    modelPart.pivotZ = position.pivotZ();
                    modelPart.roll = position.roll();
                    modelPart.yaw = position.yaw();
                    modelPart.pitch = position.pitch();
                });
            } else {
                children.forEach((name, modelPart) -> {
                    ModelPosition position = new ModelPosition(modelPart.pivotX, modelPart.pivotY, modelPart.pivotZ, modelPart.roll, modelPart.yaw, modelPart.pitch);
                    positions.put(name, position);
                });

                PlayroomClient.FROZEN_MODEL.put(livingEntity, positions);
            }
        }
    }
}
