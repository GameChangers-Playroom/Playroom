package io.github.flameyheart.playroom.mixin.client;

import io.github.flameyheart.playroom.duck.client.ExpandedClientLoginNetworkHandler;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientLoginNetworkHandler.class)
public class ClientLoginNetworkHandlerMixin implements ExpandedClientLoginNetworkHandler {
    private @Shadow @Final ClientConnection connection;

    @Override
    public void playroom$disconnect(Text reason) {
        this.connection.disconnect(reason);
    }
}
