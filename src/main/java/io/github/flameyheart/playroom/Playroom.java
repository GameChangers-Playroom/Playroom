package io.github.flameyheart.playroom;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.ConfigField;
import dev.isxander.yacl3.config.v2.api.ReadOnlyFieldAccess;
import io.github.flameyheart.playroom.command.FireworkCommand;
import io.github.flameyheart.playroom.command.PlayroomCommand;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.config.annotations.SendToClient;
import io.github.flameyheart.playroom.duck.AimingEntity;
import io.github.flameyheart.playroom.duck.ExpandedServerLoginNetworkHandler;
import io.github.flameyheart.playroom.duck.FreezableEntity;
import io.github.flameyheart.playroom.entity.LaserProjectileEntity;
import io.github.flameyheart.playroom.event.LivingEntityEvents;
import io.github.flameyheart.playroom.item.LaserGun;
import io.github.flameyheart.playroom.mixin.GsonConfigSerializerAccessor;
import io.github.flameyheart.playroom.registry.Entities;
import io.github.flameyheart.playroom.registry.Items;
import io.github.flameyheart.playroom.registry.Particles;
import io.github.flameyheart.playroom.registry.Sounds;
import io.github.flameyheart.playroom.tiltify.Automation;
import io.github.flameyheart.playroom.tiltify.Donation;
import io.github.flameyheart.playroom.tiltify.ExecuteAction;
import io.github.flameyheart.playroom.tiltify.TiltifyWebhookConnection;
import io.github.flameyheart.playroom.util.InventorySlot;
import io.github.flameyheart.playroom.util.PredicateUtils;
import io.github.flameyheart.playroom.util.ScheduleUtils;
import io.github.flameyheart.playroom.util.ThreadUtils;
import io.wispforest.owo.command.EnumArgumentType;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.parsers.json.JsonReader;
import org.quiltmc.parsers.json.gson.GsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Playroom implements ModInitializer {
	public static final String MOD_ID = "playroom";
	public static final Logger LOGGER = LoggerFactory.getLogger("Playroom");
	public static final EnumArgumentType<Donation.Status> DONATION_STATUS_ARGUMENT = EnumArgumentType.create(Donation.Status.class);
	public static final EnumArgumentType<Donation.Reward.Status> REWARD_STATUS_ARGUMENT = EnumArgumentType.create(Donation.Reward.Status.class);
	private static final Map<ServerLoginNetworkHandler, CompletableFuture<Object>> HANDSHAKE_QUEUE = new LinkedHashMap<>();
	public static final Map<UUID, Donation> DONATIONS = new LinkedHashMap<>();
	public static final Map<UUID, InventorySlot> GUN_STACKS = new LinkedHashMap<>();
	private static final Map<String, Boolean> EXPERIMENTS = new HashMap<>();
	private static final Queue<ExecuteAction> TASK_QUEUE = new LinkedList<>();
	private static TiltifyWebhookConnection sslServer;
	private static ScheduleUtils scheduler;
	private static MinecraftServer server;
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
			Playroom.scheduler = new ScheduleUtils();
		});
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ThreadUtils.tryStart(sslServer = TiltifyWebhookConnection.create());
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			ThreadUtils.interrupt(sslServer);
			if (!TASK_QUEUE.isEmpty()) {
				LOGGER.warn("There are still {} tasks in the queue, they will be lost!", TASK_QUEUE.size());
				while (!TASK_QUEUE.isEmpty()) {
					ExecuteAction action = TASK_QUEUE.poll();
					LOGGER.warn("Lost task: {} [{}]", action.task().name(), action.player());
				}
			}
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			Playroom.server = null;
			Playroom.serverTime = 0;
			Playroom.scheduler.close();
			Playroom.scheduler = null;
		});
		LivingEntityEvents.END_BASE_TICK.register(livingEntity -> {
			livingEntity.getWorld().getProfiler().push("playroom_freezing");
			FreezableEntity entity = (FreezableEntity) livingEntity;
			entity.playroom$tick();
			livingEntity.getWorld().getProfiler().pop();
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			PlayroomCommand.register(dispatcher);
			FireworkCommand.register(dispatcher);
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (server.isSingleplayer()) return;
			ServerPlayerEntity player = handler.player;
			if (!Permissions.check(player, "playroom.admin.server.update_config", 4)) {
				return;
			}
			PacketByteBuf byteBuf = PacketByteBufs.create();
			byteBuf.writeString(serializeConfig(true));
			ServerPlayNetworking.send(player, id("config/sync"), byteBuf);
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeLong(server.getOverworld().getTime());
			ServerPlayNetworking.send(player, id("time_sync"), buf);
		});
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			if (entity instanceof LaserProjectileEntity projectile && projectile.getRemovalReason() == Entity.RemovalReason.UNLOADED_TO_CHUNK) {
				projectile.remove(Entity.RemovalReason.DISCARDED);
			}
		});
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			if (!alive) {
				InventorySlot slot = GUN_STACKS.remove(oldPlayer.getUuid());
				if (slot == null) return;
				newPlayer.getInventory().setStack(slot.slot(), slot.stack());
			}
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			Playroom.scheduler.tick(server);
			long time = server.getOverworld().getTime();
			if (ThreadUtils.isAlive(sslServer) && time % 1200 == 0) {
				sslServer.requestsLastMinute = 0;
			}
			Playroom.serverTime = time;
			if (time % 100 == 0 && !getServer().isSingleplayer()) {
				PacketByteBuf buf = PacketByteBufs.create();
				buf.writeLong(time);
				sendPacket(id("time_sync"), buf);
			}

			if (!TASK_QUEUE.isEmpty()) {
				ExecuteAction action = TASK_QUEUE.poll();
				Automation.Task<?> task = action.task();
				ServerPlayerEntity player = action.player();
				boolean success = task.execute(player);
				if (!success) {
					String playerName = player == null ? "Server" : player.getName().getString();
					Playroom.sendToPlayers(p -> p.sendMessage(Text.translatable("feedback.playroom.webhook.execution.failed", task.name(), playerName)), PredicateUtils.permission("playroom.webhook.fail", 4));
					LOGGER.warn("Failed to execute webhook action '{}' for player '{}'", task.name(), playerName);
				}
			}
		});
	}

	private void handlePlayPackets() {
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
		ServerPlayNetworking.registerGlobalReceiver(id("aiming"), (server, player, handler, buf, responseSender) -> {
			boolean aiming = buf.readBoolean();

			server.execute(() -> {
				AimingEntity entity = (AimingEntity) player;
				entity.playroom$setAiming(aiming);
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(id("swap_mode"), (server, player, handler, buf, responseSender) -> {
			server.execute(() -> {
				ItemStack stack = player.getMainHandStack();
				if (stack.isEmpty() || !stack.isOf(Items.LASER_GUN)) return;
				try {
					LaserGun item = (LaserGun) stack.getItem();
					item.swapMode(player, Hand.MAIN_HAND, stack, player.getWorld());
				} catch (ClassCastException e) {
					LOGGER.error("Item '{}' is not an instance of {}!", stack.getItem().getTranslationKey(), LaserGun.class.getCanonicalName());
				}
			});
		});
	}

	private void handleLoginPackets() {
		ServerLoginNetworking.registerGlobalReceiver(id("handshake"), (server, handler, understood, buf, synchronizer, responseSender) -> {
			byte protocolVersion;
			String modVersion;
			if (buf.readableBytes() < 1) {
				protocolVersion = -1;
				modVersion = "";
			} else {
				protocolVersion = buf.readByte();
				if (protocolVersion >= 2) {
					modVersion = buf.readString();
				} else {
					modVersion = "";
				}
			}
			server.execute(() -> {
				CompletableFuture<Object> future = HANDSHAKE_QUEUE.remove(handler);
				if (future == null) {
					handler.disconnect(Text.translatable("playroom.multiplayer.disconnect.unexpected_handshake"));
					return;
				}

				if (understood) {
					if (protocolVersion == -1 && !ServerConfig.instance().allowVanillaPlayers) {
						handler.disconnect(Text.translatable("playroom.multiplayer.disconnect.unsupported_client"));
					}
					if (protocolVersion != Constants.PROTOCOL_VERSION) {
						if (ServerConfig.instance().requireMatchingProtocol) {
							handler.disconnect(Text.translatable("playroom.multiplayer.disconnect.protocol_mismatch"));
						} else {
							LOGGER.warn("Client {} has a different protocol version (Received: {} | Current: {}), they may experience inconsistent behavior", ((ExpandedServerLoginNetworkHandler) handler).playroom$getSimpleConnectionInfo(), protocolVersion, Constants.PROTOCOL_VERSION);
							responseSender.sendPacket(id("warning/mismatch/protocol"), PacketByteBufs.create());
						}
					}
					if (!modVersion.equals(getModVersion())) {
						if (ServerConfig.instance().requireMatchingVersion) {
							handler.disconnect(Text.translatable("playroom.multiplayer.disconnect.protocol_mismatch"));
						} else {
							LOGGER.warn("Client {} has a different mod version (Received: {} | Current: {}), they may experience inconsistent behavior", ((ExpandedServerLoginNetworkHandler) handler).playroom$getSimpleConnectionInfo(), modVersion, getModVersion());
							responseSender.sendPacket(id("warning/mismatch/version"), PacketByteBufs.create());
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
		ServerLoginNetworking.registerGlobalReceiver(id("warning/mismatch/version"), (server, handler, understood, buf, synchronizer, responseSender) -> {});

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
			byteBuf.writeLong(serverTime);
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
		buf.encode(NbtOps.INSTANCE, Donation.CODEC, donation);

		sendPacket(id("donation"), buf);
	}

	public static boolean hasDonation(UUID id) {
		return DONATIONS.containsKey(id);
	}

	public static void updateDonation(Donation donation) {
		DONATIONS.put(donation.id(), donation);
		PacketByteBuf buf = PacketByteBufs.create();
		buf.encode(NbtOps.INSTANCE, Donation.CODEC, donation);

		sendPacket(id("donation"), buf, PredicateUtils.permission("playroom.admin.server.update_donations", 4));
	}

	public static Donation getDonation(UUID id) {
		return DONATIONS.get(id);
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
		sendToPlayers(p -> ServerPlayNetworking.send(p, id, bufBuilder.apply(p)), predicate);
	}

	public static void sendPacketToPlayer(Identifier id, ServerPlayerEntity player, PacketByteBuf buf) {
		sendPacketToPlayer(id, player, buf, p -> true);
	}

	public static void sendPacketToPlayer(Identifier id, ServerPlayerEntity player, PacketByteBuf buf, Predicate<Entity> predicate) {
		sendPacketToPlayer(id, player, p -> buf, predicate);
	}

	public static void sendPacketToPlayer(Identifier id, ServerPlayerEntity player, Function<PlayerEntity, PacketByteBuf> bufBuilder, Predicate<Entity> predicate) {
		if (predicate.test(player)) {
			ServerPlayNetworking.send(player, id, bufBuilder.apply(player));
		}
	}

	public static void sendToPlayers(Consumer<ServerPlayerEntity> task) {
		sendToPlayers(task, p -> true);
	}

	public static void sendToPlayers(Consumer<ServerPlayerEntity> task, Predicate<Entity> predicate) {
		for (ServerPlayerEntity player : getServer().getPlayerManager().getPlayerList()) {
			if (predicate.test(player)) {
				task.accept(player);
			}
		}
	}

	public static void sendToPlayer(String playerName, Consumer<ServerPlayerEntity> task) {
		sendToPlayer(playerName, task, p -> true);
	}

	public static void sendToPlayer(String playerName, Consumer<ServerPlayerEntity> task, Predicate<Entity> predicate) {
		ServerPlayerEntity player = getServer().getPlayerManager().getPlayer(playerName);
		if (player != null && predicate.test(player)) {
			task.accept(player);
		}
	}

	public static void setExperimentStatus(String experiment, boolean status) {
		EXPERIMENTS.put(experiment, status);
	}

	public static boolean toggleExperiment(String experiment) {
		boolean status = EXPERIMENTS.compute(experiment, (s, enabled) -> enabled == null || !enabled);
		if (getServer() != null) {
			sendPacket(id("experiment"), p -> {
				PacketByteBuf buf = PacketByteBufs.create();
				buf.writeString(experiment);
				buf.writeBoolean(status);
				return buf;
			}, p -> true);
		}
		return status;
	}

	public static boolean isExperimentDisabled(String experiment) {
		return EXPERIMENTS.getOrDefault(experiment, false);
	}

	public static void queueTask(ExecuteAction task) {
		TASK_QUEUE.add(task);
	}

	public static boolean isSSLEnabled() {
		return sslServer != null && sslServer.isAlive();
	}

	public static void schedule(Runnable task, long delay, UUID uuid) {
		scheduler.schedule(server, uuid, delay, task);
	}

	public static void scheduleOrExtend(Runnable task, long delay, UUID uuid) {
		scheduler.scheduleOrExtend(server, uuid, delay, task);
	}

	@NotNull
	public static String getModVersion() {
		return FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getVersion().getFriendlyString();
	}

	public static void reload(boolean restartWebhookServer) {
		ServerConfig.INSTANCE.load();
		if (restartWebhookServer) {
			if (sslServer != null) sslServer.interrupt();
			ThreadUtils.tryStart(sslServer = TiltifyWebhookConnection.create());
		}
		sendPacket(id("config/sync"), p -> {
			boolean canModify = Permissions.check(p, "playroom.admin.server.update_config", 4);
			return PacketByteBufs.create().writeString(serializeConfig(canModify));
		}, p -> true);
	}

	public static boolean isDev() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}
}
