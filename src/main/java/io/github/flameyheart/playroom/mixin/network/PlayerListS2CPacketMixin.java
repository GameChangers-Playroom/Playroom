package io.github.flameyheart.playroom.mixin.network;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.duck.PlayerDisplayName;
import io.github.flameyheart.playroom.util.ModData;
import io.github.flameyheart.playroom.util.TextUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

@Mixin(PlayerListS2CPacket.class)
public class PlayerListS2CPacketMixin {

    @Inject(method = "<init>(Lnet/minecraft/network/packet/s2c/play/PlayerListS2CPacket$Action;Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At("TAIL"))
    private void sendPlayerNamePacket$1(PlayerListS2CPacket.Action action, ServerPlayerEntity player, CallbackInfo ci) {
        if (ModData.isModLoaded("luckperms") && ModData.isModLoaded("luckperms-placeholders") && ModData.isModLoaded("styled-nicknames")) {
            PacketByteBuf buf = PacketByteBufs.create();
            PlayerDisplayName displayName = new PlayerDisplayName(player.getUuid(),
              TextUtil.placeholder("%luckperms:prefix%", player),
              TextUtil.placeholder("%styled-nicknames:display_name%", player));
            buf.writeCollection(List.of(displayName), (buf1, displayName1) -> {
                buf1.writeUuid(displayName1.player());
                buf1.writeText(displayName1.prefix());
                buf1.writeText(displayName1.displayName());
            });
            Playroom.sendPacket(Playroom.id("player_name"), buf);
        }
    }

    @Inject(method = "<init>(Ljava/util/EnumSet;Ljava/util/Collection;)V", at = @At("TAIL"))
    private void sendPlayerNamePacket$2(EnumSet<PlayerListS2CPacket.Action> actions, Collection<ServerPlayerEntity> players, CallbackInfo ci) {
        if (ModData.isModLoaded("luckperms") && ModData.isModLoaded("styled-nicknames")) {
            PacketByteBuf buf = PacketByteBufs.create();
            List<PlayerDisplayName> displayNames = players.stream()
              .map(player -> new PlayerDisplayName(player.getUuid(),
                TextUtil.placeholder("%luckperms:prefix%", player),
                TextUtil.placeholder("%styled-nicknames:display_name%", player)))
              .toList();
            buf.writeCollection(displayNames, (buf1, displayName1) -> {
                buf1.writeUuid(displayName1.player());
                buf1.writeText(displayName1.prefix());
                buf1.writeText(displayName1.displayName());
            });
            Playroom.sendPacket(Playroom.id("player_name"), buf);
        }
    }
}
