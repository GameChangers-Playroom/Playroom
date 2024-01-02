package io.github.flameyheart.playroom.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.flameyheart.playroom.Playroom;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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
                    Boolean restartWebhookServer = context.getArgument("restart webhook server", Boolean.class);
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
                    String experiment = context.getArgument("experiment", String.class);
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
                for (EntityType<?> type : Registries.ENTITY_TYPE) {
                    Entity entity = type.create(world);
                    if (entity == null) continue;
                    entity.discard();
                    if (!(entity instanceof LivingEntity)) continue;
                    Identifier id = Registries.ENTITY_TYPE.getId(type);
                    count++;
                    world.setBlockState(pos, commandBlock);
                    ((CommandBlockBlockEntity) world.getBlockEntity(pos)).getCommandExecutor().setCommand("summon " + id.toString() + " ~ ~2 ~ {NoAI:1,Silent:1,Invulnerable:1}");
                    world.setBlockState(pos.south(), button);
                    pos = pos.west(2);
                }

                pos = player.getBlockPos().east(1);
                world.setBlockState(pos, commandBlock);
                ((CommandBlockBlockEntity) world.getBlockEntity(pos)).getCommandExecutor().setCommand("fill ~-2 ~-1 ~ ~-%d ~-1 ~ redstone_block".formatted(count * 2 + 2));
                world.setBlockState(pos.south(), button);
                pos = pos.east();
                world.setBlockState(pos, commandBlock);
                ((CommandBlockBlockEntity) world.getBlockEntity(pos)).getCommandExecutor().setCommand("fill ~-3 ~-1 ~ ~-%d ~-1 ~ air".formatted(count * 2 + 3));
                pos = pos.east(2);
                world.setBlockState(pos, Blocks.REPEATING_COMMAND_BLOCK.getDefaultState().with(CommandBlock.FACING, Direction.SOUTH));
                ((CommandBlockBlockEntity) world.getBlockEntity(pos)).getCommandExecutor().setCommand("execute as @e[type=!player] run data modify entity @s Playroom.TicksFrozen set value 150");
                world.setBlockState(pos.south(), Blocks.LEVER.getDefaultState().with(WallMountedBlock.FACING, Direction.SOUTH));

                int finalCount = count;
                source.sendFeedback(() -> Text.translatable("commands.playroom.entity_test", finalCount), false);

                return count;
            })
          )
        );
    }
}
