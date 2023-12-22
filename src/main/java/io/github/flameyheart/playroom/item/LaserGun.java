package io.github.flameyheart.playroom.item;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.entity.LaserProjectileEntity;
import io.github.flameyheart.playroom.mixin.geo.AnimationControllerAccessor;
import io.github.flameyheart.playroom.registry.Sounds;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Vanishable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LaserGun extends Item implements Vanishable, FabricItem, GeoItem, PlayroomItem, Aimable {
    private static final RawAnimation RAPIDFIRE_MODE_ANIMATION = RawAnimation.begin().thenPlayAndHold("animation.model.rapidfire");
    private static final RawAnimation RANGE_MODE_ANIMATION = RawAnimation.begin().thenPlayAndHold("animation.model.range");
    private static final RawAnimation RAPIDFIRE_CHARGE_ANIMATION = RawAnimation.begin().thenPlay("animation.model.rapidfire.fire");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
    private final List<TooltipProvider> tooltipProvider = new ArrayList<>();
    private Supplier<Boolean> showAdvancedTooltip = () -> false;
    private Object renderer = null;

    public LaserGun(Settings settings) {
        super(settings);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        tooltipProvider.add((stack, world, tooltip, context) -> {
           if (showAdvancedTooltip.get()) {
               if (isCooldownExpired(stack)) {
                   if (isRapidFire(stack)) {
                       tooltip.add(Text.translatable("item.playroom.laser_gun.tooltip.amo", getAmo(stack)));
                   } else {
                       tooltip.add(Text.translatable("item.playroom.laser_gun.tooltip.ready"));
                   }
               } else {
                   int cooldown = getCooldownLeft(stack);
                   Object timeLeft;
                   if (getCooldownLeft(stack) < 20) {
                       timeLeft = (float) Math.floor((cooldown / 20f) * 10) / 10;
                   } else {
                       timeLeft = cooldown / 20;
                   }
                   tooltip.add(Text.translatable("item.playroom.laser_gun.tooltip.cooldown", timeLeft));
               }
           }
        });

        tooltipProvider.add((stack, world, tooltip, context) -> {
            if (!showAdvancedTooltip.get()) {
                tooltip.add(Text.translatable("item.playroom.laser_gun.tooltip.more"));
            }
        });
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.playroom.laser_gun.tooltip.regen"));
        tooltipProvider.forEach(provider -> provider.appendTooltip(stack, world, tooltip, context));
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (world instanceof ServerWorld serverWorld) {
            boolean rapidFire = getPlayroomTag(stack).getBoolean("RapidFire");

            if (rapidFire) {
                if (getPlayroomTag(stack).getByte("Amo") <= 0 && isCooldownExpired(stack)) {
                    getPlayroomTag(stack).putByte("Amo", ServerConfig.instance().laserRapidFireAmo);
                }

                long geoId = GeoItem.getOrAssignId(stack, serverWorld);
                AnimationController<GeoAnimatable> controller = getAnimatableInstanceCache().getManagerForId(geoId).getAnimationControllers().get("controller");
                if (((AnimationControllerAccessor) controller).getTriggeredAnimation() == null) {
                    triggerAnim(entity, geoId, "controller", "rapidfire_mode");
                }
            }
        }
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (remainingUseTicks < 72000 - 20 && !isRapidFire(stack)) {
            handleRangedMode(stack, world, (PlayerEntity) user, Hand.MAIN_HAND);
        }
        getPlayroomTag(stack).putInt("Charge", 0);
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
    }

    public int getMaxUseTime(ItemStack stack) {
        return isRapidFire(stack) ? 0 : 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.NONE;
    }

    public int getUseTime(ItemStack stack, int remainingUseTicks) {
        return this.getMaxUseTime(stack) - remainingUseTicks;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (user instanceof PlayerEntity player && canFire(stack, Hand.MAIN_HAND, player)) {
            if (isRapidFire(stack)) {
                handleRapidFire(world, player, Hand.MAIN_HAND, stack);
            } else {
                int charge = MathHelper.clamp(getUseTime(stack, remainingUseTicks) * 4, 0, 100);
                getPlayroomTag(stack).putLong("Charge", charge);
            }
        }
        super.usageTick(world, user, stack, remainingUseTicks);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, @NotNull PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!canUse(stack, hand)) {
            return TypedActionResult.fail(stack);
        } else {
            if (!player.isSneaking()) {
                if (isRapidFire(stack)) {
                    handleRapidFire(world, player, hand, stack);
                } else {
                    playSound(world, player, Sounds.LASER_GUN_CHARGE);
                }
            }

            handleUseLogic(player, hand, stack, world);
            player.setCurrentHand(hand);
            return TypedActionResult.pass(stack);
        }
    }

    private boolean canUse(ItemStack stack, Hand hand) {
        return isCooldownExpired(stack) && hand == Hand.MAIN_HAND;
    }

    private boolean canFire(ItemStack stack, Hand hand, PlayerEntity player) {
        return canUse(stack, hand) && !player.isSneaking();
    }

    private void handleUseLogic(PlayerEntity player, Hand hand, ItemStack stack, World world) {
        if (!canUse(stack, hand)) return;
        boolean rapidFire = getPlayroomTag(stack).getBoolean("RapidFire");

        if (player.isSneaking()) {
            getPlayroomTag(stack).putBoolean("RapidFire", !rapidFire);
            setCooldown(stack, ServerConfig.instance().laserSwapModeCooldown);

            if (world instanceof ServerWorld serverWorld) {
                SoundEvent soundEvent = rapidFire ? SoundEvents.BLOCK_PISTON_CONTRACT : SoundEvents.BLOCK_PISTON_EXTEND;
                world.playSound(null, player.getX(), player.getY(), player.getZ(), soundEvent, SoundCategory.PLAYERS, 0.5F, 2.0F);
                long geoId = GeoItem.getOrAssignId(stack, serverWorld);
                if (rapidFire) {
                    triggerAnim(player, geoId, "controller", "range_mode");
                } else {
                    triggerAnim(player, geoId, "controller", "rapidfire_mode");
                }
            }
        }
    }

    private void handleRangedMode(ItemStack stack, World world, PlayerEntity player, Hand hand) {
        if (!canFire(stack, hand, player)) return;

        setCooldown(stack, ServerConfig.instance().laserFireReloadTime);
        getPlayroomTag(stack).putByte("Amo", (byte) (-1));

        if (world instanceof ServerWorld serverWorld) {
            fireProjectile(player, false, serverWorld);
            playShootSound(world, player, false);
        }
    }

    private void handleRapidFire(World world, @NotNull PlayerEntity player, Hand hand, ItemStack stack) {
        if (!canFire(stack, hand, player)) return;

        boolean rapidFire = getPlayroomTag(stack).getBoolean("RapidFire");
        if (!rapidFire) return;

        short amo = (short) (getPlayroomTag(stack).getShort("Amo") - 1);
        getPlayroomTag(stack).putShort("Amo", amo);

        if (amo <= 0) {
            setCooldown(stack, ServerConfig.instance().laserFireReloadTime);
        }

        if (world instanceof ServerWorld serverWorld) {
            fireProjectile(player, true, serverWorld);
            playShootSound(world, player, true);
        }
    }

    public void fireProjectile(PlayerEntity player, boolean rapidFire, ServerWorld world) {
        LaserProjectileEntity laserShot = LaserProjectileEntity.create(world, player, rapidFire);
        laserShot.setVelocity(player, player.getPitch(), player.getHeadYaw(), 0.0f, getProjectileSpeed(rapidFire), getProjectileDivergence(rapidFire));
        world.spawnEntity(laserShot);
    }

    public void playShootSound(World world, PlayerEntity player, boolean rapidFire) {
        SoundEvent soundEvent = rapidFire ? Sounds.LASER_GUN_SHOOT_RAPID : Sounds.LASER_GUN_SHOOT_RANGE;
        world.playSound(null, player.getX(), player.getY(), player.getZ(), soundEvent, SoundCategory.PLAYERS, 0.5F, 1.0F);
    }

    @Override
    public int getItemBarStep(@NotNull ItemStack stack) {
        long cooldown = getCooldownTag(stack).getLong("Duration");
        long cooldownExpires = getCooldownTag(stack).getLong("ExpireTick");
        long time = Playroom.getServer().getOverworld().getTime();
        long timeLeft = cooldownExpires - time;

        if (timeLeft <= 0 && getPlayroomTag(stack).getBoolean("RapidFire")) {
            return Math.round(getPlayroomTag(stack).getByte("Amo") * 13.0F / (float) ServerConfig.instance().laserRapidFireAmo);
        }

        return Math.round(13.0F - timeLeft * 13.0F / (float) cooldown);
    }

    @Override
    public int getItemBarColor(@NotNull ItemStack stack) {
        long cooldown = getCooldownTag(stack).getLong("Duration");
        long cooldownExpires = getCooldownTag(stack).getLong("ExpireTick");
        long time = Playroom.getServer().getOverworld().getTime();
        long timeLeft = cooldownExpires - time;

        if (timeLeft <= 0 && getPlayroomTag(stack).getBoolean("RapidFire")) return 0x0000FF;

        float f = Math.max(0f, ((float) cooldown - timeLeft) / (float) cooldown);

        return MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isItemBarVisible(@NotNull ItemStack stack) {
        if (getPlayroomTag(stack).getBoolean("RapidFire") && isCooldownExpired(stack)) {
            return getPlayroomTag(stack).getByte("Amo") > 0;
        }

        return !isCooldownExpired(stack);
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    public void setRenderer(Object renderer) {
        if (this.renderer == null) {
            this.renderer = renderer;
        }
    }

    public void registerTooltipProvider(TooltipProvider tooltipProvider) {
        this.tooltipProvider.add(tooltipProvider);
    }

    public void setShowAdvancedTooltip(Supplier<Boolean> showAdvancedTooltip) {
        this.showAdvancedTooltip = showAdvancedTooltip;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        if (renderer != null) {
            consumer.accept(renderer);
        }
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }

    @Override
    public void registerControllers(@NotNull AnimatableManager.ControllerRegistrar controllerRegistrar) {
        AnimationController<GeoAnimatable> controller = new AnimationController<>(this, "controller", 0, this::predicate);
        controller.triggerableAnim("rapidfire", RAPIDFIRE_CHARGE_ANIMATION);
        controller.triggerableAnim("rapidfire_mode", RAPIDFIRE_MODE_ANIMATION);
        controller.triggerableAnim("range_mode", RANGE_MODE_ANIMATION);
        controllerRegistrar.add(controller);
    }

    private PlayState predicate(@NotNull AnimationState<GeoAnimatable> animationState) {
        //animationState.getController().setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    public void setCooldown(ItemStack stack, int time) {
        if (time > 0) {
            getCooldownTag(stack).putInt("Duration", time);
            getCooldownTag(stack).putLong("ExpireTick", Playroom.getServer().getOverworld().getTime() + time);
        }
    }

    public boolean isCooldownExpired(ItemStack stack) {
        return getCooldownTag(stack).getLong("ExpireTick") < Playroom.getServer().getOverworld().getTime();
    }

    public int getCooldownLeft(ItemStack stack) {
        return (int) (getCooldownTag(stack).getLong("ExpireTick") - Playroom.getServer().getOverworld().getTime());
    }

    public int getAmo(ItemStack stack) {
        return getPlayroomTag(stack).getByte("Amo");
    }

    private float getProjectileSpeed(boolean rapidFire) {
        return rapidFire ? ServerConfig.instance().laserRapidBulletSpeed : ServerConfig.instance().laserRangedBulletSpeed;
    }

    private float getProjectileDivergence(boolean rapidFire) {
        return rapidFire ? ServerConfig.instance().laserRapidDivergence : ServerConfig.instance().laserRangedDivergence;
    }

    public boolean isRapidFire(ItemStack stack) {
        return getPlayroomTag(stack).getBoolean("RapidFire");
    }

    @Override
    public boolean canAim(ItemStack stack) {
        return !isRapidFire(stack);
    }

    @FunctionalInterface
    public interface TooltipProvider {
        void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context);
    }
}
