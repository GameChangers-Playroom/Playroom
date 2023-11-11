package io.github.flameyheart.playroom;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.ConfigField;
import dev.isxander.yacl3.config.v2.api.ReadOnlyFieldAccess;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.config.annotations.SendToClient;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.duck.ExpandedServerLoginNetworkHandler;
import io.github.flameyheart.playroom.event.EntityTickEvents;
import io.github.flameyheart.playroom.mixin.GsonConfigSerializerAccessor;
import io.github.flameyheart.playroom.mixin.ServerLoginNetworkHandlerAccessor;
import io.github.flameyheart.playroom.registry.Items;
import io.github.flameyheart.playroom.registry.Particles;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Playroom implements ModInitializer {
	public static final String MOD_ID = "playroom";
    public static final Logger LOGGER = LoggerFactory.getLogger("Playroom");
	private static final Map<ServerLoginNetworkHandler, CompletableFuture<Object>> HANDSHAKE_QUEUE = new LinkedHashMap<>();

	private static MinecraftServer server;

	@Override
	public void onInitialize() {
		ServerConfig.INSTANCE.serializer().load();
		FieldRegistrationHandler.register(Items.class, MOD_ID, false);
		FieldRegistrationHandler.register(Particles.class, MOD_ID, false);
		Items.ITEM_GROUP.initialize();

		registerEventListeners();
		handlePlayPackets();
		handleLoginPackets();
	}

	private void registerEventListeners() {
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
						eEntity.playroom$setGunFreezeTicks(Math.max(0, freezeTicks - entity.getFireTicks()));
						entity.setFireTicks(0);
					}

					eEntity.playroom$setGunFreezeTicks(Math.max(0, freezeTicks - 1));
				}
				entity.getWorld().getProfiler().pop();
			}
		});
	}

	private void handlePlayPackets() {
		ServerPlayNetworking.registerGlobalReceiver(id("dev/freeze_player"), (server, player, handler, buf, responseSender) -> {
			if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
				LOGGER.warn("Using dev feature in non-dev environment is not supported!");
				LOGGER.warn("Also please bonk Awakened for forgetting to remove this");
				return;
			}
			ExpandedEntityData entity = (ExpandedEntityData) player;
			entity.playroom$setGunFreezeTicks(entity.playroom$freezeTime());
		});

		ServerPlayNetworking.registerGlobalReceiver(id("config/update"), (server, player, handler, buf, responseSender) -> {
			if (!Permissions.check(player, "playroom.admin.server.update_config", 4)) {
				LOGGER.warn("Player {} tried to update the server config without permission!", player.getName());

				return;
			}
			ExpandedEntityData entity = (ExpandedEntityData) player;
			entity.playroom$setGunFreezeTicks(entity.playroom$freezeTime());
		});
	}

	private void handleLoginPackets() {
		ServerLoginNetworking.registerGlobalReceiver(id("handshake"), (server, handler, understood, buf, synchronizer, responseSender) -> {
			byte protocolVersion = buf.readByte();
			server.execute(() -> {
				CompletableFuture<Object> future = HANDSHAKE_QUEUE.remove(handler);
				if (future == null) {
					handler.disconnect(Text.translatable("playroom.multiplayer.disconnect.unexpected_handshake"));
					return;
				}

				if (understood) {
					if (protocolVersion != Constants.PROTOCOL_VERSION) {
						if (getConfig().requireMatchingProtocol) {
							handler.disconnect(Text.translatable("playroom.multiplayer.disconnect.protocol_mismatch"));
						} else {
							LOGGER.warn("Client {} has a different protocol version ({}), they may experience inconsistent behavior", ((ExpandedServerLoginNetworkHandler) handler).playroom$getSimpleConnectionInfo(), protocolVersion);
							responseSender.sendPacket(id("warning/mismatch/protocol"), PacketByteBufs.create());
						}
					}
					future.complete(null);
				} else {
					if (!ServerConfig.instance().allowVanillaPlayers) {
						handler.disconnect(Text.translatable("playroom.multiplayer.disconnect.unsupported_client"));
					}
				}
			});
		});

		ServerLoginNetworking.registerGlobalReceiver(id("warning/mismatch/protocol"), (server, handler, understood, buf, synchronizer, responseSender) -> {});

		ServerLoginConnectionEvents.DISCONNECT.register((handler, server) -> {
			HANDSHAKE_QUEUE.remove(handler);
		});

		ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
			CompletableFuture<Object> future = new CompletableFuture<>();

			if (HANDSHAKE_QUEUE.containsKey(handler)) {
				HANDSHAKE_QUEUE.remove(handler);
				handler.disconnect(Text.translatable("playroom.multiplayer.disconnect.unexpected_query_start"));
				return;
			}

			boolean isOp = server.getPlayerManager().isOperator(((ServerLoginNetworkHandlerAccessor) handler).getProfile());

			HANDSHAKE_QUEUE.put(handler, future);

			synchronizer.waitFor(future);
			PacketByteBuf byteBuf = PacketByteBufs.create();
			byteBuf.writeString(serializeConfig(isOp));
			sender.sendPacket(id("handshake"), byteBuf);
		});
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	public static MinecraftServer getServer() {
		return server;
	}

	public static ServerConfig getConfig() {
		return ServerConfig.INSTANCE.instance();
	}

	public static ServerConfig getConfigDefault() {
		return ServerConfig.INSTANCE.defaults();
	}

	public static String serializeConfig(boolean sendAll) {
		ConfigClassHandler<ServerConfig> config = ServerConfig.INSTANCE;
		Gson gson = ((GsonConfigSerializerAccessor) config.serializer()).getGson();
		JsonObject json = new JsonObject();

		for (ConfigField<?> field : config.fields()) {
			ReadOnlyFieldAccess<?> defaultedAccess = field.defaultAccess();
			if (sendAll || defaultedAccess.getAnnotation(SendToClient.class).isPresent()) {
				json.add(defaultedAccess.name(), gson.toJsonTree(defaultedAccess.get()));
			}
		}

		try {
			//Use custom writer to minify the JSON, we don't want to send unnecessary data to the client
			StringWriter writer = new StringWriter();
			JsonWriter jsonWriter = gson.newJsonWriter(Streams.writerForAppendable(writer));
			jsonWriter.setIndent("");
			gson.toJson(json, jsonWriter);
			return writer.toString();
		} catch (IOException e) {
			throw new JsonIOException(e);
		}
	}
}
