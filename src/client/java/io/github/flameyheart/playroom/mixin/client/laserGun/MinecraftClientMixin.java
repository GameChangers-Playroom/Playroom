package io.github.flameyheart.playroom.mixin.client.laserGun;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flameyheart.playroom.item.Aimable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow @Nullable public ClientPlayerEntity player;

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemEnabled(Lnet/minecraft/resource/featuretoggle/FeatureSet;)Z"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void cancelOffhand(CallbackInfo ci, Hand[] var1, int var2, int var3, Hand hand, ItemStack itemStack) {
        if (this.player != null && player.getMainHandStack().getItem() instanceof Aimable && hand == Hand.OFF_HAND) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/HitResult;getType()Lnet/minecraft/util/hit/HitResult$Type;"))
    private HitResult.Type cancelInteractions(HitResult instance, Operation<HitResult.Type> original) {
        if (player == null) return original.call(instance);
        ItemStack stack = this.player.getMainHandStack();
        if (stack.getItem() instanceof Aimable) {
            return HitResult.Type.MISS;
        }
        return original.call(instance);
    }

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void cancelAttack(CallbackInfoReturnable<Boolean> cir) {
        if (this.player != null && this.player.getMainHandStack().getItem() instanceof Aimable) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void cancelBreaking(boolean breaking, CallbackInfo ci) {
        if (this.player != null && this.player.getMainHandStack().getItem() instanceof Aimable) {
            ci.cancel();
        }
    }
}
