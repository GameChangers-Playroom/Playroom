package io.github.flameyheart.playroom.item;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.mixin.geo.AnimationControllerAccessor;
import io.github.flameyheart.playroom.util.Raycast;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Vanishable;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LaserGun extends Item implements Vanishable, FabricItem, GeoItem, PlayroomItem, Aimable {
    private static final RawAnimation RAPIDFIRE_MODE_ANIMATION = RawAnimation.begin().thenPlayAndHold("animation.model.rapidfire");
    private static final RawAnimation RANGE_MODE_ANIMATION = RawAnimation.begin().thenPlayAndHold("animation.model.range");
    private static final RawAnimation RAPIDFIRE_CHARGE_ANIMATION = RawAnimation.begin().thenPlay("animation.model.rapidfire.fire");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
    private Object renderer = null;

    public LaserGun(Settings settings) {
        super(settings);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.empty());
        tooltip.add(Text.translatable("item.playroom.laser_gun.tooltip"));
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (world instanceof ServerWorld serverWorld) {
            boolean rapid = getPlayroomTag(stack).getBoolean("RapidFire");

            if (rapid) {
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
        //user.sendMessage(Text.literal("message.playroom.laser_gun"));
    }

    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.NONE;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, @NotNull PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (hand == Hand.OFF_HAND) {
            return TypedActionResult.fail(stack);
        }

        Vec3d start = player.getCameraPosVec(0).add(0, -0.05, 0);

        if (getCooldownTag(stack).getLong("ExpireTick") > Playroom.getServer().getOverworld().getTime()) {
            return TypedActionResult.fail(stack);
        }

        HitResult raycast = Raycast.raycast(world, player, ServerConfig.instance().laserReach, false, true);

        short cooldownTime;
        boolean rapid = getPlayroomTag(stack).getBoolean("RapidFire");
        if (player.isSneaking()) {
            getPlayroomTag(stack).putBoolean("RapidFire", !rapid);
            cooldownTime = ServerConfig.instance().swapModeCooldown;

            getCooldownTag(stack).putShort("Duration", cooldownTime);
            getCooldownTag(stack).putLong("ExpireTick", Playroom.getServer().getOverworld().getTime() + cooldownTime);
        }/* else {
            if (raycast.getType() == HitResult.Type.ENTITY && ((EntityHitResult) raycast).getEntity() instanceof PlayerEntity target) {
                cooldownTime = ServerConfig.instance().laserHitReloadTime;
                target.setFrozenTicks(target.getMinFreezeDamageTicks() + 200);
            } else {
                cooldownTime = ServerConfig.instance().laserMissReloadTime;
            }
        }*/

        if (world instanceof ServerWorld serverWorld) {
            if (player.isSneaking()) {
                long geoId = GeoItem.getOrAssignId(player.getStackInHand(hand), serverWorld);
                if (rapid) {
                    triggerAnim(player, geoId, "controller", "range_mode");
                } else {
                    triggerAnim(player, geoId, "controller", "rapidfire_mode");
                }
                return TypedActionResult.pass(stack);
            }

            /*double distance = Math.sqrt(raycast.getPos().squaredDistanceTo(start));

            Vec3d direction = player.getRotationVec(0);

            for (double i = 0; i < distance; i += 0.01 * distance) {
                Vec3d end = start.add(direction.x * i, direction.y * i, direction.z * i);
                spawnParticles(serverWorld, new DustParticleEffect(Vec3d.unpackRgb(0xFF0000).toVector3f(), 1.0f),
                  end.x, end.y, end.z, 1, 0, 0, 0, 1);

            }*/
            //player.swingHand(hand, true);
        }

        player.setCurrentHand(hand);
        return TypedActionResult.pass(stack);
    }

    protected <T extends ParticleEffect> void spawnParticles(@NotNull ServerWorld world, T particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
        ParticleS2CPacket particleS2CPacket = new ParticleS2CPacket(particle, true, x, y, z, (float)deltaX, (float)deltaY, (float)deltaZ, (float)speed, count);
        for (int i = 0; i < world.getPlayers().size(); ++i) {
            ServerPlayerEntity serverPlayerEntity = world.getPlayers().get(i);
            world.sendToPlayerIfNearby(serverPlayerEntity, true, x, y, z, particleS2CPacket);
        }
    }

    @Override
    public int getItemBarStep(@NotNull ItemStack stack) {
        long cooldown = getCooldownTag(stack).getLong("Duration");
        long cooldownExpires = getCooldownTag(stack).getLong("ExpireTick");
        long time = Playroom.getServer().getOverworld().getTime();
        long timeLeft = cooldownExpires - time;

        return Math.round(13.0F - timeLeft * 13.0F / (float) cooldown);
    }

    @Override
    public int getItemBarColor(@NotNull ItemStack stack) {
        long cooldown = getCooldownTag(stack).getLong("Duration");
        long cooldownExpires = getCooldownTag(stack).getLong("ExpireTick");
        long time = Playroom.getServer().getOverworld().getTime();
        long timeLeft = cooldownExpires - time;

        float f = Math.max(0f, ((float) cooldown - timeLeft) / (float) cooldown);

        return MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isItemBarVisible(@NotNull ItemStack stack) {
        return getCooldownTag(stack).getLong("ExpireTick") > Playroom.getServer().getOverworld().getTime();
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
}
