package io.github.flameyheart.playroom.mixin;

import io.github.flameyheart.playroom.duck.Client2ServerPlayerSettings;
import io.github.flameyheart.playroom.duck.FreezableEntity;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements Client2ServerPlayerSettings {
    @Unique
    private ClientSettingsC2SPacket packet;

    @Inject(method = "updateInput", at = @At("HEAD"), cancellable = true)
    private void ignoreInputUpdates(float sidewaysSpeed, float forwardSpeed, boolean jumping, boolean sneaking, CallbackInfo ci) {
        if (((FreezableEntity) this).playroom$isFrozen()) {
            ci.cancel();
        }
    }

    @Inject(method = "setClientSettings", at = @At("TAIL"))
    private void getClientSettingsPacket(ClientSettingsC2SPacket packet, CallbackInfo ci) {
        this.packet = packet;
    }

    @Override
    public int playroom$getViewDistance() {
        if(packet == null)
            return 1;
        
        return Math.max(packet.viewDistance(), 1);
    }

}
