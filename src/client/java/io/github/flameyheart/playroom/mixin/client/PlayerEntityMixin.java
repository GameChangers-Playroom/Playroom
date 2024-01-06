package io.github.flameyheart.playroom.mixin.client;

import io.github.flameyheart.playroom.duck.client.FancyDisplayName;
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
public abstract class PlayerEntityMixin extends LivingEntity {

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	//TODO: Improve
	@Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
	public void getDisplayName(CallbackInfoReturnable<Text> ci) {
		Object self = this;
		if (self instanceof FancyDisplayName player) {
			if (player.playroom$hasDisplayName()) {
				ci.setReturnValue(player.playroom$getDisplayName());
			}
		}
	}
}
