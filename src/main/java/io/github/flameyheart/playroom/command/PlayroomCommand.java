package io.github.flameyheart.playroom.command;

import com.bawnorton.trulyrandom.api.TrulyRandomApi;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.command.argument.RewardArgumentType;
import io.github.flameyheart.playroom.duck.FreezableEntity;
import io.github.flameyheart.playroom.registry.Damage;
import io.github.flameyheart.playroom.tiltify.Automation;
import io.github.flameyheart.playroom.tiltify.Donation;
import io.github.flameyheart.playroom.tiltify.Reward;
import io.github.flameyheart.playroom.util.LinedStringBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PlayroomCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
          literal("playroom").requires(Permissions.require("playroom.command.playroom", 2)).then(
            literal("reload").requires(Permissions.require("playroom.command.playroom.reload", 4)).executes(context -> {
                  context.getSource().sendFeedback(() -> Text.translatable("commands.playroom.reload"), false);
                  Playroom.reload(false);
                  return 1;
              }
            ).then(
              argument("restart webhook server", BoolArgumentType.bool()).executes(context -> {
                    boolean restartWebhookServer = BoolArgumentType.getBool(context, "restart webhook server");
                    context.getSource().sendFeedback(() -> Text.translatable("commands.playroom.reload"), false);
                    if (restartWebhookServer) {
                        context.getSource().sendFeedback(() -> Text.translatable("commands.playroom.reload.restart_webhook_server"), false);
                    }
                    Playroom.reload(restartWebhookServer);
                    return 1;
                }
              )
            ).requires(Permissions.require("playroom.command.playroom.reload.restart", 4))
          ).then(
            literal("experiment").requires(Permissions.require("playroom.command.playroom.experiment", Playroom.isDev())).then(
              argument("experiment", StringArgumentType.word()).executes(context -> {
                    String experiment = StringArgumentType.getString(context, "experiment");
                    context.getSource().sendFeedback(() -> Text.translatable("commands.playroom.experiment", experiment, Playroom.toggleExperiment(experiment)), false);
                    return 1;
                }
              )
            )
          ).then(
            literal("check-permission").requires(Permissions.require("playroom.command.playroom.check-permission", Playroom.isDev())).then(
              argument("target", EntityArgumentType.entity()).then(
                argument("permission", StringArgumentType.word()).executes(context -> {
                    ServerCommandSource source = context.getSource();
                    Entity target = EntityArgumentType.getEntity(context, "target");
                    String permission = StringArgumentType.getString(context, "permission");

                    boolean check = Permissions.check(source, permission);

                    source.sendFeedback(() -> Text.translatable("commands.playroom.permission", target.getDisplayName(), permission, check), false);
                    return check ? 1 : 0;
                })
              )
            )
          ).then(
            literal("entity-test").requires(Permissions.require("playroom.command.playroom.entity-test", Playroom.isDev())).executes(context -> {
                ServerCommandSource source = context.getSource();
                if (!Playroom.isDev()) {
                    source.sendError(Text.literal("This command is only available in development environment.\nThis is to avoid accidental mass destruction."));
                    return 0;
                }

                ServerPlayerEntity player = source.getPlayer();
                if (player == null) return 0;
                World world = player.getWorld();
                BlockPos pos = player.getBlockPos().west();
                int count = 0;
                BlockState commandBlock = Blocks.COMMAND_BLOCK.getDefaultState().with(CommandBlock.FACING, Direction.SOUTH);
                BlockState button = Blocks.STONE_BUTTON.getDefaultState().with(WallMountedBlock.FACING, Direction.SOUTH);
                BlockState sign = Blocks.OAK_WALL_SIGN.getDefaultState();
                for (EntityType<?> type : Registries.ENTITY_TYPE) {
                    Entity entity = type.create(world);
                    if (entity == null) continue;
                    entity.discard();
                    if (!(entity instanceof LivingEntity)) continue;
                    Identifier id = Registries.ENTITY_TYPE.getId(type);
                    count++;
                    world.setBlockState(pos, commandBlock);
                    ((CommandBlockBlockEntity) world.getBlockEntity(pos)).getCommandExecutor().setCommand("summon " + id + " ~ ~2 ~ {NoAI:1,Silent:1,Invulnerable:1,PersistenceRequired:1,NoGravity:1}");
                    world.setBlockState(pos.south(), button);
                    world.setBlockState(pos = pos.up(), sign);
                    var signEntity = ((SignBlockEntity) world.getBlockEntity(pos));
                    SignText text = signEntity.getText(false).withGlowing(true).withColor(DyeColor.CYAN);
                    signEntity.setText(text.withMessage(1, Text.translatable(entity.getType().getTranslationKey())), false);
                    signEntity.setWaxed(true);
                    pos = pos.down().west(2);
                }

                pos = player.getBlockPos().east(1);
                world.setBlockState(pos, commandBlock);
                ((CommandBlockBlockEntity) world.getBlockEntity(pos)).getCommandExecutor().setCommand("fill ~-2 ~-1 ~ ~-%d ~-1 ~ redstone_block".formatted(count * 2 + 2));
                world.setBlockState(pos.south(), button);
                world.setBlockState(pos = pos.up(), sign);
                var signEntity = ((SignBlockEntity) world.getBlockEntity(pos));
                SignText text = signEntity.getText(false).withGlowing(true).withColor(DyeColor.CYAN);
                signEntity.setText(text.withMessage(1, Text.literal("Summon all")), false);
                signEntity.setWaxed(true);
                pos = pos.down().east();

                world.setBlockState(pos, commandBlock);
                ((CommandBlockBlockEntity) world.getBlockEntity(pos)).getCommandExecutor().setCommand("fill ~-3 ~-1 ~ ~-%d ~-1 ~ air".formatted(count * 2 + 3));
                pos = pos.east(2);

                world.setBlockState(pos, Blocks.REPEATING_COMMAND_BLOCK.getDefaultState().with(CommandBlock.FACING, Direction.SOUTH));
                ((CommandBlockBlockEntity) world.getBlockEntity(pos)).getCommandExecutor().setCommand("execute as @e[type=!entity] run playroom test freeze @s");
                world.setBlockState(pos.south(), Blocks.LEVER.getDefaultState().with(WallMountedBlock.FACING, Direction.SOUTH));
                world.setBlockState(pos = pos.up(), sign);
                signEntity = ((SignBlockEntity) world.getBlockEntity(pos));
                text = signEntity.getText(false).withGlowing(true).withColor(DyeColor.CYAN);
                signEntity.setText(text.withMessage(1, Text.literal("Freeze all")), false);
                signEntity.setWaxed(true);

                int finalCount = count;
                source.sendFeedback(() -> Text.translatable("commands.playroom.entity_test", finalCount), false);

                return count;
            })
          ).then(
            literal("status").requires(Permissions.require("playroom.command.playroom.status", 4)).executes(context -> {
                ServerCommandSource source = context.getSource();
                LinedStringBuilder stringBuilder = new LinedStringBuilder();
                stringBuilder.append("Webhook server: ");
                stringBuilder.append(Playroom.isSSLEnabled() ? "Running" : "Offline");
                stringBuilder.appendLine("Donations received: ");
                stringBuilder.append(Playroom.DONATIONS.size());

                source.sendFeedback(() -> Text.literal(stringBuilder.toString()), false);

                return 1;
            })
          ).then(
            literal("test").requires(Permissions.require("playroom.command.playroom.test", 2)).then(
              literal("freeze").requires(Permissions.require("playroom.command.playroom.test.freeze", 2)).then(
                argument("target", EntityArgumentType.entity()).executes(context -> {
                      ServerCommandSource source = context.getSource();
                      Entity entity = EntityArgumentType.getEntity(context, "target");
                      if (!(entity instanceof FreezableEntity)) {
                          source.sendError(Text.translatable("commands.playroom.error.not_freezable", entity.getDisplayName()));
                          return 0;
                      }
                      ((FreezableEntity) entity).playroom$freeze();
                      ((FreezableEntity) entity).playroom$freeze();
                      source.sendFeedback(() -> Text.translatable("commands.playroom.test.freeze", entity.getDisplayName()), false);
                      return 1;
                  }
                )
              ).requires(Permissions.require("playroom.command.playroom.test.freeze.target", 2)).executes(context -> {
                    ServerCommandSource source = context.getSource();
                    if (!source.isExecutedByPlayer()) {
                        source.sendError(Text.translatable("commands.playroom.error.not_player"));
                        return 0;
                    }
                    ServerPlayerEntity player = source.getPlayer();
                    ((FreezableEntity) player).playroom$freeze();
                    source.sendFeedback(() -> Text.translatable("commands.playroom.test.freeze", player.getDisplayName()), false);
                    return 1;
                }
              )
            ).then(
              literal("slowdown").requires(Permissions.require("playroom.command.playroom.test.slowdown", 2)).then(
                argument("target", EntityArgumentType.entity()).executes(context -> {
                      ServerCommandSource source = context.getSource();
                      Entity entity = EntityArgumentType.getEntity(context, "target");
                      if (!(entity instanceof FreezableEntity)) {
                          source.sendError(Text.translatable("commands.playroom.error.not_freezable", entity.getDisplayName()));
                          return 0;
                      }
                      ((FreezableEntity) entity).playroom$slowdown();
                      source.sendFeedback(() -> Text.translatable("commands.playroom.test.slowdown", entity.getDisplayName()), false);
                      return 1;
                  }
                )
              ).requires(Permissions.require("playroom.command.playroom.test.slowdown.target", 2)).executes(context -> {
                    ServerCommandSource source = context.getSource();
                    if (!source.isExecutedByPlayer()) {
                        source.sendError(Text.translatable("commands.playroom.error.not_player"));
                        return 0;
                    }
                    ServerPlayerEntity player = source.getPlayer();
                    ((FreezableEntity) player).playroom$slowdown();
                    source.sendFeedback(() -> Text.translatable("commands.playroom.test.slowdown", player.getDisplayName()), false);
                    return 1;
                }
              )
            ).then(
              literal("damage").requires(Permissions.require("playroom.command.playroom.test.damage", 2)).then(
                argument("target", EntityArgumentType.entity()).executes(context -> {
                      ServerCommandSource source = context.getSource();
                      Entity player = EntityArgumentType.getEntity(context, "target");
                      player.damage(Damage.laserShot(player.getWorld(), null, null), Float.MAX_VALUE);
                      return 1;
                  }
                )
              ).requires(Permissions.require("playroom.command.playroom.test.damage.target", 2)).executes(context -> {
                    ServerCommandSource source = context.getSource();
                    if (!source.isExecutedByPlayer()) {
                        source.sendError(Text.translatable("commands.playroom.error.not_player"));
                        return 0;
                    }
                    ServerPlayerEntity player = source.getPlayer();
                    player.damage(Damage.laserShot(player.getWorld(), null, null), Float.MAX_VALUE);
                    return 1;
                }
              )
            ).then(
              literal("fire").requires(Permissions.require("playroom.command.playroom.test.fire", 2)).then(
                argument("time", IntegerArgumentType.integer(0)).executes(context -> {
                    ServerCommandSource source = context.getSource();
                    if (!source.isExecutedByPlayer()) {
                        source.sendError(Text.translatable("commands.playroom.error.not_player"));
                        return 0;
                    }
                    ServerPlayerEntity player = source.getPlayer();
                    player.setFireTicks(IntegerArgumentType.getInteger(context, "time"));
                    return 1;
                })
              ).then(
                argument("target", EntityArgumentType.entity()).then(argument("time", IntegerArgumentType.integer(0)).executes(context -> {
                      ServerCommandSource source = context.getSource();
                      Entity player = EntityArgumentType.getEntity(context, "target");
                      player.setFireTicks(IntegerArgumentType.getInteger(context, "time"));
                      return 1;
                  })
                )
              ).requires(Permissions.require("playroom.command.playroom.test.fire.target", 2))
            )
          ).then(
            literal("donation").requires(Permissions.require("playroom.command.playroom.donation", 2)).then(
              argument("donation id", StringArgumentType.word()).then(
                argument("status", Playroom.DONATION_STATUS_ARGUMENT).suggests(Playroom.DONATION_STATUS_ARGUMENT::listSuggestions).executes(context -> {
                    ServerCommandSource source = context.getSource();
                    UUID donationId = UUID.fromString(StringArgumentType.getString(context, "donation id"));
                    Donation.Status status = context.getArgument("status", Donation.Status.class);
                    if (!Playroom.hasDonation(donationId)) {
                        source.sendError(Text.translatable("commands.playroom.error.donation.not_found", donationId.toString()));
                        return -1;
                    }
                    Donation donation = Playroom.getDonation(donationId);
                    donation.updateStatus(status);
                    source.sendFeedback(() -> Text.translatable("commands.playroom.donation.status", donationId.toString(), status.name()), true);

                    Playroom.updateDonation(donation);
                    return 1;
                })
              ).then(
                argument("reward id", StringArgumentType.word()).then(
                  argument("status", Playroom.REWARD_STATUS_ARGUMENT).suggests(Playroom.REWARD_STATUS_ARGUMENT::listSuggestions).executes(context -> {
                      ServerCommandSource source = context.getSource();
                      UUID donationId = UUID.fromString(StringArgumentType.getString(context, "donation id"));
                      UUID rewardId = UUID.fromString(StringArgumentType.getString(context, "reward id"));
                      Donation.Reward.Status status = context.getArgument("status", Donation.Reward.Status.class);
                      if (!Playroom.hasDonation(donationId)) {
                          source.sendError(Text.translatable("commands.playroom.error.donation.not_found", donationId.toString()));
                          return -1;
                      }
                      Donation donation = Playroom.getDonation(donationId);
                      Donation.Reward reward = donation.reward(rewardId);
                      reward.updateStatus(status);
                      source.sendFeedback(() -> Text.translatable("commands.playroom.donation.reward.status", donationId.toString(), rewardId.toString(), status.name()), true);

                      boolean error = false;
                      for (Donation.Reward rewardInfo : donation.rewards()) {
                          if (rewardInfo.status().error) {
                              error = true;
                              break;
                          }
                      }

                      if (!error) {
                          donation.updateStatus(Donation.Status.NORMAL);
                      }

                      Playroom.updateDonation(donation);
                      return 1;
                  })
                )
              )
            )
          ).then(
            literal("apply-reward").requires(Permissions.require("playroom.command.playroom.apply-reward", 4)).then(
              argument("reward", RewardArgumentType.reward()).then(
                argument("player", EntityArgumentType.player()).executes(context -> {
                    ServerCommandSource source = context.getSource();
                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                    Reward reward = RewardArgumentType.getReward(context, "reward");
                    Automation.Task<?> task = Automation.get(reward.uuid());
                    if (!task.requiresPlayer()) {
                        source.sendError(Text.translatable("commands.playroom.error.reward.requires_no_player", reward.displayName()));
                        return 0;
                    }
                    boolean success = task.execute(player);
                    if (success) {
                        source.sendFeedback(() -> Text.translatable("commands.playroom.reward.success", reward.displayName(), player.getDisplayName()), true);
                    } else {
                        source.sendError(Text.translatable("commands.playroom.reward.error", reward.displayName(), player.getDisplayName()));
                        return 0;
                    }
                    return 1;
                })
              ).executes(context -> {
                  ServerCommandSource source = context.getSource();
                  ServerPlayerEntity player = source.getPlayer();
                  Reward reward = RewardArgumentType.getReward(context, "reward");
                  Automation.Task<?> task = Automation.get(reward.uuid());
                  if (task.requiresPlayer()) {
                      source.sendError(Text.translatable("commands.playroom.error.reward.requires_player", reward.displayName()));
                      return 0;
                  }
                  boolean success = task.execute(null);
                  if (success) {
                      source.sendFeedback(() -> Text.translatable("commands.playroom.reward.success", reward.displayName(), "the server"), true);
                  } else {
                      source.sendError(Text.translatable("commands.playroom.reward.error", reward.displayName(), "the server"));
                      return 0;
                  }
                  return 1;
              })
            )
          )
        );
    }
}
