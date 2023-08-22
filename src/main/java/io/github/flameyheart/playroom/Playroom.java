package io.github.flameyheart.playroom;

import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.event.EntityTickEvents;
import io.github.flameyheart.playroom.registry.Items;
import io.github.flameyheart.playroom.registry.Particles;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Playroom implements ModInitializer {
	public static final String MOD_ID = "playroom";
    public static final Logger LOGGER = LoggerFactory.getLogger("Playroom");

	private static MinecraftServer server;

	@Override
	public void onInitialize() {
		FieldRegistrationHandler.register(Items.class, MOD_ID, false);
		FieldRegistrationHandler.register(Particles.class, MOD_ID, false);
		Items.ITEM_GROUP.initialize();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> Playroom.server = server);

		EntityTickEvents.END_BASE_TICK.register(baseEntity -> {
			if (baseEntity instanceof PlayerEntity entity) {
				entity.getWorld().getProfiler().push("playroom_freezing");
				ExpandedEntityData eEntity = (ExpandedEntityData) entity;

				int freezeTicks = eEntity.playroom$getGunFreezeTicks();

				if (!entity.getWorld().isClient && !entity.isDead() && freezeTicks > 0) {
					if (entity.hasPassengers()) entity.removeAllPassengers();
					if (entity.hasVehicle()) entity.stopRiding();
					if (entity.isFallFlying()) entity.stopFallFlying();
					entity.dropShoulderEntities();

					if (entity.isOnFire()) {
						entity.setFireTicks(0);
						eEntity.playroom$setGunFreezeTicks(Math.max(0, freezeTicks - 100));
					}

					eEntity.playroom$setGunFreezeTicks(Math.max(0, freezeTicks - 1));
				}
				entity.getWorld().getProfiler().pop();
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(id("dev/freeze_player"), (server, player, handler, buf, responseSender) -> {
			if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
				LOGGER.warn("Using dev feature in non-dev environment is not supported!");
				LOGGER.warn("Also please bonk Awakened for forgetting to remove this");
				return;
			}
			ExpandedEntityData entity = (ExpandedEntityData) player;
			entity.playroom$setGunFreezeTicks(entity.playroom$maxFrozenTime());
		});
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	public static MinecraftServer getServer() {
		return server;
	}
}
