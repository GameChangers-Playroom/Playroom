package io.github.flameyheart.playroom.mixin.client.geo;

import io.github.flameyheart.playroom.PlayroomClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import software.bernie.geckolib.network.packet.AnimTriggerPacket;
import software.bernie.geckolib.util.RenderUtils;

@Environment(EnvType.CLIENT)
@Mixin(AnimTriggerPacket.class)
public class AnimTriggerPacketMixin {

    @Inject(method = "receive", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void setAnimationStartTick(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender, CallbackInfo ci, String syncableId, long instanceId, String controllerName, String animName) {
        PlayroomClient.ANIMATION_START_TICK.put(instanceId,  RenderUtils.getCurrentTick());
    }
}
