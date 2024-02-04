package io.github.flameyheart.playroom.item;

import io.github.flameyheart.playroom.config.ServerConfig;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Deprecated
public class RewriteLaserGun extends Item implements Aimable, FabricItem, GeoItem {

	private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	private Object renderer = null;

	public RewriteLaserGun(Settings settings) {
		super(settings);
	}

	public void setRenderer(Object renderer) {
		if (this.renderer == null)
			this.renderer = renderer;
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return null;
	}

	@Override
	public Supplier<Object> getRenderProvider() {
		return renderProvider;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.NONE;
	}

	@Override
	public boolean canAim(ItemStack stack) {
		return false;
	}

	@Override
	public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
		return false;
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return ServerConfig.instance().laserRangeChargeTime;
	}

	@Override
	public void createRenderer(Consumer<Object> consumer) {
		if (renderer != null)
			consumer.accept(renderer);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

	}

}
