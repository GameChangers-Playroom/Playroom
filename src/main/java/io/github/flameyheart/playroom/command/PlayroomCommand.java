package io.github.flameyheart.playroom.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.flameyheart.playroom.Playroom;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

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
          )
        );
    }
}
