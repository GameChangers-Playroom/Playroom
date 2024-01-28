package io.github.flameyheart.playroom.mixin.client.freeze.render;

import io.github.flameyheart.playroom.duck.FreezableEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {

    @Inject(method = "drawEntity(Lnet/minecraft/client/gui/DrawContext;IIIFFLnet/minecraft/entity/LivingEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(Lnet/minecraft/client/gui/DrawContext;IIILorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/entity/LivingEntity;)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    private static void disableMouseFacing(DrawContext context, int x, int y, int size, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci, float f, float g, Quaternionf quaternionf, Quaternionf quaternionf2, float h, float i, float j, float k, float l) {
        if (entity instanceof FreezableEntity playroomEntity && playroomEntity.playroom$isFrozen()) {
            Quaternionf rotateZ = new Quaternionf().rotateZ((float)Math.PI);
            Quaternionf rotateX = new Quaternionf();
            entity.bodyYaw = 180;
            entity.setYaw(180);
            entity.setPitch(0);
            entity.headYaw = entity.getYaw();
            entity.prevHeadYaw = entity.getYaw();
            InventoryScreen.drawEntity(context, x, y, size, rotateZ, rotateX, entity);

            entity.bodyYaw = h;
            entity.setYaw(i);
            entity.setPitch(j);
            entity.prevHeadYaw = k;
            entity.headYaw = l;
            ci.cancel();
        }
    }

    // TODO: Make entity model freeze to the angle too, else default to facing center
    @Inject(method = "drawEntity(Lnet/minecraft/client/gui/DrawContext;IIIFFLnet/minecraft/entity/LivingEntity;)V", at = @At(value = "INVOKE", target = "Lorg/joml/Quaternionf;mul(Lorg/joml/Quaternionfc;)Lorg/joml/Quaternionf;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    private static void freezeEntity(DrawContext context, int x, int y, int size, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci, float f, float g, Quaternionf quaternionf, Quaternionf quaternionf2) {
        if (entity instanceof FreezableEntity playroomEntity && playroomEntity.playroom$isFrozen()) {
//            entity.bodyYaw = h;
//            entity.setYaw(i);
//            entity.setPitch(j);
//            entity.headYaw = k;
//            entity.prevHeadYaw = l;
            InventoryScreen.drawEntity(context, x, y, size, quaternionf, new Quaternionf(), entity);
            
            ci.cancel();
        }
    }
}
