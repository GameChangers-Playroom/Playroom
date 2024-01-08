package io.github.flameyheart.playroom.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.duck.FreezableEntity;
import io.github.flameyheart.playroom.registry.Damage;
import io.github.flameyheart.playroom.util.LinedStringBuilder;
import net.fabricmc.loader.api.FabricLoader;
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

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PlayroomCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
          literal("playroom").then(
            literal("reload").executes(context -> {
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
            )
          ).then(
            literal("experiment").then(
              argument("experiment", StringArgumentType.word()).executes(context -> {
                    String experiment = StringArgumentType.getString(context, "experiment");
                    context.getSource().sendFeedback(() -> Text.translatable("commands.playroom.experiment", experiment, Playroom.setExperiment(experiment)), false);
                    return 1;
                }
              )
            )
          ).then(
            literal("entity-test").executes(context -> {
                ServerCommandSource source = context.getSource();
                if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
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
                ((CommandBlockBlockEntity) world.getBlockEntity(pos)).getCommandExecutor().setCommand("execute as @e[type=!player] run playroom test freeze @s");
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
            literal("status").executes(context -> {
                ServerCommandSource source = context.getSource();
                LinedStringBuilder stringBuilder = new LinedStringBuilder();
                stringBuilder.append("Webhook server: ");
                stringBuilder.appendLine(Playroom.isSSLEnabled() ? "Running" : "Offline");
                stringBuilder.append("Donations received: ");
                stringBuilder.appendLine(Playroom.DONATIONS.size());

                source.sendFeedback(() -> Text.literal(stringBuilder.toString()), false);

                return 1;
            })
          ).then(
            literal("test").then(
              literal("freeze").then(
                argument("target", EntityArgumentType.entity()).executes(context -> {
                      ServerCommandSource source = context.getSource();
                      Entity player = EntityArgumentType.getEntity(context, "target");
                      ((FreezableEntity) player).playroom$freeze();
                      source.sendFeedback(() -> Text.translatable("commands.playroom.test.freeze", player.getDisplayName()), false);
                      return 1;
                  }
                )
              ).executes(context -> {
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
              literal("slowdown").then(
                argument("target", EntityArgumentType.entity()).executes(context -> {
                      ServerCommandSource source = context.getSource();
                      Entity player = EntityArgumentType.getEntity(context, "target");
                      ((FreezableEntity) player).playroom$slowdown();
                      source.sendFeedback(() -> Text.translatable("commands.playroom.test.slowdown", player.getDisplayName()), false);
                      return 1;
                  }
                )
              ).executes(context -> {
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
              literal("damage").then(
                argument("target", EntityArgumentType.entity()).executes(context -> {
                      ServerCommandSource source = context.getSource();
                      Entity player = EntityArgumentType.getEntity(context, "target");
                      player.damage(Damage.laserShot(player.getWorld(), null, null), Float.MAX_VALUE);
                      return 1;
                  }
                )
              ).executes(context -> {
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
            )
          )
        );
    }
}
