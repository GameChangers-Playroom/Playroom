package io.github.flameyheart.playroom.mixin;

import com.mojang.authlib.GameProfile;
import io.github.flameyheart.playroom.duck.ExpandedServerLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerLoginNetworkHandler.class)
public interface ServerLoginNetworkHandlerAccessor {
    @Accessor GameProfile getProfile();
    //@Accessor ClientConnection getConnection();
}
