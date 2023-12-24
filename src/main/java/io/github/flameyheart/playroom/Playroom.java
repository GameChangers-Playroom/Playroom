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
import io.github.flameyheart.playroom.entity.LaserProjectileEntity;
import io.github.flameyheart.playroom.event.EntityTickEvents;
import io.github.flameyheart.playroom.mixin.GsonConfigSerializerAccessor;
import io.github.flameyheart.playroom.mixin.PlayerEntityInvoker;
import io.github.flameyheart.playroom.registry.Entities;
import io.github.flameyheart.playroom.registry.Items;
import io.github.flameyheart.playroom.registry.Particles;
import io.github.flameyheart.playroom.registry.Sounds;
import io.github.flameyheart.playroom.tiltify.Donation;
import io.github.flameyheart.playroom.tiltify.TiltifyWebhookConnection;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.quiltmc.parsers.json.JsonReader;
import org.quiltmc.parsers.json.gson.GsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Playroom implements ModInitializer {
	public static final String MOD_ID = "playroom";
    public static final Logger LOGGER = LoggerFactory.getLogger("Playroom");
	private static final Map<ServerLoginNetworkHandler, CompletableFuture<Object>> HANDSHAKE_QUEUE = new LinkedHashMap<>();
	public static final Map<UUID, Donation> DONATIONS = new LinkedHashMap<>();
	private static TiltifyWebhookConnection sslServer;

	private static MinecraftServer server;
	private static boolean stopTicking = false;
	public static long serverTime = 0;

	@Override
	public void onInitialize() {
		ServerConfig.INSTANCE.serializer().load();
		FieldRegistrationHandler.register(Items.class, MOD_ID, false);
		FieldRegistrationHandler.register(Particles.class, MOD_ID, false);
		FieldRegistrationHandler.register(Entities.class, MOD_ID, false);
		FieldRegistrationHandler.register(Sounds.class, MOD_ID, false);
		Items.ITEM_GROUP.initialize();

		registerEventListeners();
		handlePlayPackets();
		handleLoginPackets();
	}

	private void registerEventListeners() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			Playroom.server = server;
		});
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			sslServer = new TiltifyWebhookConnection();
			sslServer.start();
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			sslServer.interrupt();
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			Playroom.server = null;
		});
		EntityTickEvents.END_BASE_TICK.register(baseEntity -> {
			if (stopTicking) return;
			if (baseEntity instanceof PlayerEntity entity) {
				entity.getWorld().getProfiler().push("playroom_freezing");
				ExpandedEntityData eEntity = (ExpandedEntityData) entity;

				int freezeTicks = eEntity.playroom$getGunFreezeTicks();

				if (!entity.getWorld().isClient && !entity.isDead() && freezeTicks > 0) {
					if (entity.hasPassengers()) entity.removeAllPassengers();
					if (entity.hasVehicle()) entity.stopRiding();
					if (entity.isFallFlying()) entity.stopFallFlying();
					((PlayerEntityInvoker) entity).invokeDropShoulderEntities();

					if (entity.isOnFire()) {
						eEntity.playroom$setGunFreezeTicks(Math.max(0, freezeTicks - entity.getFireTicks()));
						entity.setFireTicks(0);
					}

					eEntity.playroom$setGunFreezeTicks(Math.max(0, freezeTicks - 1));
				}
				entity.getWorld().getProfiler().pop();
			}
		});
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			ServerConfig.INSTANCE.load();
			if (sslServer != null) sslServer.interrupt();
			sslServer = new TiltifyWebhookConnection();
			sslServer.start();
			sendPacket(id("config/sync"), p -> {
				boolean canModify = Permissions.check(p, "playroom.admin.server.update_config", 4);
				return PacketByteBufs.create().writeString(serializeConfig(canModify));
			}, p -> true);
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			server.execute(() -> {
				if (!Permissions.check(handler.player, "playroom.admin.server.update_config", 4)) {
					return;
				}
				PacketByteBuf byteBuf = PacketByteBufs.create();
				byteBuf.writeString(serializeConfig(true));
				ServerPlayNetworking.send(handler.player, id("config/sync"), byteBuf);
			});
		});
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			if (entity instanceof LaserProjectileEntity projectile && projectile.getRemovalReason() == Entity.RemovalReason.UNLOADED_TO_CHUNK) {
				projectile.remove(Entity.RemovalReason.DISCARDED);
			}
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			long time = server.getOverworld().getTime();
			if (time % 100 == 0) {
				PacketByteBuf buf = PacketByteBufs.create();
				buf.writeLong(time);
				sendPacket(id("time_sync"), buf);
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
			entity.playroom$freeze();
		});
		ServerPlayNetworking.registerGlobalReceiver(id("dev/freeze_player/add"), (server, player, handler, buf, responseSender) -> {
			if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
				LOGGER.warn("Using dev feature in non-dev environment is not supported!");
				LOGGER.warn("Also please bonk Awakened for forgetting to remove this");
				return;
			}
			ExpandedEntityData entity = (ExpandedEntityData) player;
			entity.playroom$addGunFreezeTicks(1);
		});
		ServerPlayNetworking.registerGlobalReceiver(id("dev/freeze_player/rem"), (server, player, handler, buf, responseSender) -> {
			if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
				LOGGER.warn("Using dev feature in non-dev environment is not supported!");
				LOGGER.warn("Also please bonk Awakened for forgetting to remove this");
				return;
			}
			ExpandedEntityData entity = (ExpandedEntityData) player;
			entity.playroom$addGunFreezeTicks(-1);
		});

		ServerPlayNetworking.registerGlobalReceiver(id("dev/toggle_ticking"), (server, player, handler, buf, responseSender) -> {
			if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
				LOGGER.warn("Using dev feature in non-dev environment is not supported!");
				LOGGER.warn("Also please bonk Awakened for forgetting to remove this");
				return;
			}
			stopTicking = !stopTicking;
		});

		ServerPlayNetworking.registerGlobalReceiver(id("config/update"), (server, player, handler, buf, responseSender) -> {
			if (!Permissions.check(player, "playroom.admin.server.update_config", 4)) {
				LOGGER.warn("Player {} tried to update the server config without permission!", player.getName());
				return;
			}
			String serverConfig = buf.readString();
			server.execute(() -> {
				if (!deserializeConfig(serverConfig)) {
					LOGGER.warn("Received corrupted or broken config data from {}", player.getName());
					server.getCommandSource().sendFeedback(() -> Text.literal("Received corrupted or broken config data from " + player.getName()), true);
					server.getCommandSource().sendFeedback(() -> Text.literal("The server config may be broken, a restart is recommended!"), true);
					return;
				}
				ServerConfig.INSTANCE.save();

				sendPacket(id("config/sync"), p -> {
					boolean canModify = Permissions.check(p, "playroom.admin.server.update_config", 4);
					return PacketByteBufs.create().writeString(serializeConfig(canModify));
				}, p -> true);
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(id("donation/update"), (server, player, handler, buf, responseSender) -> {
			if (!Permissions.check(player, "playroom.admin.server.update_donations", 4)) {
				LOGGER.warn("Player {} tried to update the server donations without permission!", player.getName());
				return;
			}
			UUID id = buf.readUuid();
			Donation.Status status = buf.readEnumConstant(Donation.Status.class);

			server.execute(() -> {
				Donation donation = DONATIONS.get(id);
				if (donation == null) {
					LOGGER.warn("Player {} tried to update a donation that doesn't exist!", player.getName());
					return;
				}
				donation.updateStatus(status);

				PacketByteBuf byteBuf = PacketByteBufs.create();
				byteBuf.writeUuid(id);
				byteBuf.writeEnumConstant(status);
				sendPacket(id("donation/update"), byteBuf, p -> Permissions.check(p, "playroom.admin.server.update_donations", 4));
			});
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
						if (ServerConfig.instance().requireMatchingProtocol) {
							handler.disconnect(Text.translatable("playroom.multiplayer.disconnect.protocol_mismatch"));
						} else {
							LOGGER.warn("Client {} has a different protocol version (Received: {} | Current: {}), they may experience inconsistent behavior", ((ExpandedServerLoginNetworkHandler) handler).playroom$getSimpleConnectionInfo(), protocolVersion, Constants.PROTOCOL_VERSION);
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

		//Empty receiver to prevent the client from getting kicked for "unknown packet"
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

			HANDSHAKE_QUEUE.put(handler, future);

			synchronizer.waitFor(future);
			PacketByteBuf byteBuf = PacketByteBufs.create();
			byteBuf.writeString(serializeConfig(false));
			sender.sendPacket(id("handshake"), byteBuf);
		});
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	public static MinecraftServer getServer() {
		return server;
	}

	public static String serializeConfig(boolean sendAll) {
		ConfigClassHandler<ServerConfig> config = ServerConfig.INSTANCE;
		Gson gson = ((GsonConfigSerializerAccessor) config.serializer()).getGson();
		JsonObject json = new JsonObject();

		for (ConfigField<?> field : config.fields()) {
			ReadOnlyFieldAccess<?> defaultedAccess = field.defaultAccess();
			ReadOnlyFieldAccess<?> access = field.access();
			if (sendAll || defaultedAccess.getAnnotation(SendToClient.class).isPresent()) {
				json.add(defaultedAccess.name(), gson.toJsonTree(field.access().get()));
			}
		}

		try {
			//Use a custom writer to minify the JSON, we don't want to send unnecessary data to the client
			StringWriter writer = new StringWriter();
			JsonWriter jsonWriter = gson.newJsonWriter(Streams.writerForAppendable(writer));
			jsonWriter.setIndent("");
			gson.toJson(json, jsonWriter);
			return writer.toString();
		} catch (IOException e) {
			throw new JsonIOException(e);
		}
	}

	//TODO: improve
	public static boolean deserializeConfig(String data) {
		if (server != null && server.isSingleplayer()) return true;
		try (JsonReader jsonReader = JsonReader.json5(data)) {
			ConfigClassHandler<ServerConfig> config = ServerConfig.INSTANCE;
			Gson gson = ((GsonConfigSerializerAccessor) config.serializer()).getGson();

			GsonReader gsonReader = new GsonReader(jsonReader);

			Map<String, ConfigField<?>> fieldMap = Arrays.stream(config.fields())
			.filter(field -> field.serial().isPresent())
			.collect(Collectors.toMap(f -> f.serial().orElseThrow().serialName(), Function.identity()));

			jsonReader.beginObject();

			while (jsonReader.hasNext()) {
				String name = jsonReader.nextName();
				ConfigField<?> field = fieldMap.get(name);
				if (field == null) {
					Playroom.LOGGER.warn("Unknown config field '{}' sent from server!", name);
					jsonReader.skipValue();
					continue;
				}

				try {
					field.access().set(gson.fromJson(gsonReader, field.access().type()));
				} catch (Exception e) {
					Playroom.LOGGER.error("Failed to deserialize config field '{}'.", name, e);
					jsonReader.skipValue();
				}
			}

			jsonReader.endObject();

			return true;
		} catch (Throwable e) {
			Playroom.LOGGER.error("Failed to decode server config!", e);
			return false;
		}
	}

	public static Path getConfigPath() {
		return FabricLoader.getInstance().getConfigDir().resolve("playroom");
	}

	public static void addDonation(Donation donation) {
		DONATIONS.put(donation.id(), donation);
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeUuid(donation.id());
		buf.writeString(donation.donorName());
		buf.writeString(donation.message());
		buf.writeFloat(donation.amount());
		buf.writeString(donation.currency());
		buf.writeBoolean(donation.status() == Donation.Status.AUTO_APPROVED);

		sendPacket(id("donation/add"), buf);
	}

	public static boolean hasDonation(UUID id) {
		return DONATIONS.containsKey(id);
	}

	public static ServerCommandSource getCommandSource() {
		ServerWorld serverWorld = server.getOverworld();
		return new ServerCommandSource(server, serverWorld == null ? Vec3d.ZERO : Vec3d.of(serverWorld.getSpawnPos()), Vec2f.ZERO,
		serverWorld, 4, "Playroom", Text.literal("Playroom"), server, null);
	}

	public static void sendPacket(Identifier id, PacketByteBuf buf) {
		sendPacket(id, buf, p -> true);
	}

	public static void sendPacket(Identifier id, PacketByteBuf buf, Predicate<Entity> predicate) {
		sendPacket(id, p -> buf, predicate);
	}

	public static void sendPacket(Identifier id, Function<PlayerEntity, PacketByteBuf> bufBuilder, Predicate<Entity> predicate) {
		for (ServerPlayerEntity player : getServer().getPlayerManager().getPlayerList()) {
			if (predicate.test(player)) {
				ServerPlayNetworking.send(player, id, bufBuilder.apply(player));
			}
		}
	}
}
