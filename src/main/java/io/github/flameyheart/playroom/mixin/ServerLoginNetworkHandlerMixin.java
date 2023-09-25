package io.github.flameyheart.playroom.mixin;

import com.mojang.authlib.GameProfile;
import io.github.flameyheart.playroom.duck.ExpandedServerLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin implements ExpandedServerLoginNetworkHandler {
    @Shadow @Nullable GameProfile profile;
    @Shadow @Final ClientConnection connection;

    @Override
    public String playroom$getSimpleConnectionInfo() {
        if (this.profile != null) {
            return String.format("%s[%s] (%s)", this.profile.getName(), this.profile.getId(), this.connection.getAddress());
        }
        return String.valueOf(this.connection.getAddress());
    }
}
