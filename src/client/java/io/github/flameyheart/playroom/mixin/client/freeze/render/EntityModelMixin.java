package io.github.flameyheart.playroom.mixin.client.freeze.render;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.render.entity.ModelPosition;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
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
    @Unique protected ModelPart playroom$root;

    @Inject(method = "<init>*", at = @At("TAIL"), require = 1)
    private void storeRoot0(CallbackInfo ci, @Local(argsOnly = true) ModelPart root) {
        //PlayroomClient.LOGGER.info("Assigning playroom$root: " + this.getClass().getSimpleName());
        if (root == null) {
            PlayroomClient.LOGGER.error("Receiver null root from constructor: " + this.getClass().getSimpleName());
        }
        this.playroom$root = root;
        if (playroom$root == null) {
            PlayroomClient.LOGGER.error("Failed to assign value to playroom$root, WTF: " + this.getClass().getSimpleName());
        }
    }


    @Inject(method = {
      "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
      "method_2819(Lnet/minecraft/class_1309;FFFFF)V",
    }, at = @At("TAIL"))
    private void stopAnimations(@Coerce Entity livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (playroom$root == null) return;
        if (!(livingEntity instanceof ExpandedEntityData entity)) {
            PlayroomClient.LOGGER.warn("Entity is not ExpandedEntityData: " + livingEntity.getClass().getSimpleName());
            return;
        }
        if (entity.playroom$showIce()) {
            Map<String, ModelPart> children = ((ModelPartAccessor) playroom$root).getChildren();
            Map<String, ModelPosition> positions = PlayroomClient.FROZEN_MODEL.computeIfAbsent(livingEntity, v -> new HashMap<>());
            if (!positions.isEmpty()) {
                positions.forEach(this::playroom$resetPositions);
            } else {
                children.forEach((name, modelPart) -> playroom$storePositions(name, modelPart, playroom$root, positions));
                PlayroomClient.FROZEN_MODEL.put(livingEntity, positions);
            }
        }
    }

    @Unique
    protected void playroom$resetPositions(String name, ModelPosition position) {
        ModelPart modelPart = position.parentPart().getChild(name);
        modelPart.pivotX = position.pivotX();
        modelPart.pivotY = position.pivotY();
        modelPart.pivotZ = position.pivotZ();
        modelPart.roll = position.roll();
        modelPart.yaw = position.yaw();
        modelPart.pitch = position.pitch();
        position.children().forEach(this::playroom$resetPositions);
    }

    @Unique
    protected void playroom$storePositions(String name, ModelPart modelPart, ModelPart parent, Map<String, ModelPosition> positions) {
        Map<String, ModelPosition> _children = new HashMap<>();
        ModelPosition position = new ModelPosition(modelPart.pivotX, modelPart.pivotY, modelPart.pivotZ, modelPart.roll, modelPart.yaw, modelPart.pitch, parent, _children);
        ((ModelPartAccessor) modelPart).getChildren().forEach((childName, childModelPart) -> playroom$storePositions(childName, childModelPart, modelPart, _children));
        positions.put(name, position);
    }
}
