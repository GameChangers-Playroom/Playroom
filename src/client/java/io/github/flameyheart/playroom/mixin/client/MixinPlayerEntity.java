package io.github.flameyheart.playroom.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerEntity.class, priority = 1010)
public abstract class MixinPlayerEntity extends LivingEntity {

	protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	//TODO: Improve
	@Inject(method="getDisplayName", at=@At("RETURN"), cancellable=true)
	public void getDisplayName(CallbackInfoReturnable<Text> ci) {
		Object self = this;
		if (self instanceof AbstractClientPlayerEntity) {
			PlayerListEntry ple = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(getUuid());
			if (ple != null && ple.getDisplayName() != null) {
				ci.setReturnValue(ple.getDisplayName());
			}
		}
	}

}
