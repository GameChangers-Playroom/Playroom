package io.github.flameyheart.playroom.mixin.client.freeze.model;

import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.duck.client.FreezableModel;
import io.github.flameyheart.playroom.render.entity.ModelPosition;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = {
        AllayEntityModel.class, ArmorStandArmorEntityModel.class, ArmorStandEntityModel.class,
        AxolotlEntityModel.class, BatEntityModel.class, BeeEntityModel.class, BipedEntityModel.class, BlazeEntityModel.class,
        CamelEntityModel.class, CatEntityModel.class, ChickenEntityModel.class, CodEntityModel.class,
        CreeperEntityModel.class, DolphinEntityModel.class, DonkeyEntityModel.class, DrownedEntityModel.class, EndermanEntityModel.class,
        EndermiteEntityModel.class, FoxEntityModel.class, FrogEntityModel.class, GhastEntityModel.class, GoatEntityModel.class,
        GuardianEntityModel.class, HoglinEntityModel.class, HorseEntityModel.class, IllagerEntityModel.class, IronGolemEntityModel.class,
        LargePufferfishEntityModel.class, LargeTropicalFishEntityModel.class, LlamaEntityModel.class, MagmaCubeEntityModel.class,
        MediumPufferfishEntityModel.class, OcelotEntityModel.class, PandaEntityModel.class, ParrotEntityModel.class, PhantomEntityModel.class,
        PiglinEntityModel.class, PolarBearEntityModel.class, RabbitEntityModel.class, RavagerEntityModel.class,
        SalmonEntityModel.class, SheepEntityModel.class, SheepWoolEntityModel.class, ShulkerEntityModel.class, SilverfishEntityModel.class,
        SkeletonEntityModel.class, SlimeEntityModel.class, SmallPufferfishEntityModel.class, SmallTropicalFishEntityModel.class,
        SnifferEntityModel.class, SnowGolemEntityModel.class, SpiderEntityModel.class, SquidEntityModel.class, StriderEntityModel.class,
        TadpoleEntityModel.class, TurtleEntityModel.class, VexEntityModel.class, VillagerResemblingModel.class, WardenEntityModel.class,
        WitchEntityModel.class, WitherEntityModel.class, WolfEntityModel.class
})
public abstract class EntityModelMixin implements FreezableModel {
    @Unique
    protected ModelPart playroom$root;

    @Inject(method = "<init>(Lnet/minecraft/client/model/ModelPart;)V", at = @At("TAIL"), require = 0)
    private void storeRoot0(ModelPart root, CallbackInfo ci) {
        captureRoot(root);
    }

    @Unique
    private void captureRoot(ModelPart root) {
        if (root == null) throw new NullPointerException("Root is null for " + this.getClass().getSimpleName());
        this.playroom$root = root;
    }

    @Override
    public ModelPart playroom$getRoot() {
        return playroom$root;
    }

    @Inject(method = {
            "*(Lnet/minecraft/entity/Entity;FFFFF)V",
            "*(Lnet/minecraft/class_1297;FFFFF)V",
    }, at = @At("TAIL"))
    private void stopAnimations(@Coerce Object entity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (entity instanceof ExpandedEntityData eEntity) {
            playroom$stopAnimation(eEntity);
        }
    }
}
