package io.github.flameyheart.playroom.config;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import io.github.flameyheart.playroom.PlayroomClient;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class TestCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("pc2")
          .then(literal("inc")
            .then(argument("value", FloatArgumentType.floatArg())
              .executes(context -> {
                  PlayroomClient.kbdInc = FloatArgumentType.getFloat(context, "value");
                  return 1;
              })
            )
          ).then(literal("target")
            .then(literal("value1")
              .executes(context -> {
                  PlayroomClient.kbd4Func = () -> ClientConfig.instance().value1 += PlayroomClient.kbdInc;
                  PlayroomClient.kbd5Func = () -> ClientConfig.instance().value1 -= PlayroomClient.kbdInc;
                  return 1;
              })
            )
          ).then(literal("target")
            .then(literal("value2")
              .executes(context -> {
                  PlayroomClient.kbd5Func = () -> ClientConfig.instance().value2 -= PlayroomClient.kbdInc;
                  PlayroomClient.kbd4Func = () -> ClientConfig.instance().value2 += PlayroomClient.kbdInc;
                  return 1;
              })
            )
          ).then(literal("target")
            .then(literal("value3")
              .executes(context -> {
                  PlayroomClient.kbd5Func = () -> ClientConfig.instance().value3 -= PlayroomClient.kbdInc;
                  PlayroomClient.kbd4Func = () -> ClientConfig.instance().value3 += PlayroomClient.kbdInc;
                  return 1;
              })
            )
          ).then(literal("target")
            .then(literal("value4")
              .executes(context -> {
                  PlayroomClient.kbd5Func = () -> ClientConfig.instance().value4 -= PlayroomClient.kbdInc;
                  PlayroomClient.kbd4Func = () -> ClientConfig.instance().value4 += PlayroomClient.kbdInc;
                  return 1;
              })
            )
          ).then(literal("target")
            .then(literal("value5")
              .executes(context -> {
                  PlayroomClient.kbd5Func = () -> ClientConfig.instance().value5 -= PlayroomClient.kbdInc;
                  PlayroomClient.kbd4Func = () -> ClientConfig.instance().value5 += PlayroomClient.kbdInc;
                  return 1;
              })
            )
          ).then(literal("target")
            .then(literal("value6")
              .executes(context -> {
                  PlayroomClient.kbd5Func = () -> ClientConfig.instance().value6 -= PlayroomClient.kbdInc;
                  PlayroomClient.kbd4Func = () -> ClientConfig.instance().value6 += PlayroomClient.kbdInc;
                  return 1;
              })
            )
          ).then(literal("target")
            .then(literal("value7")
              .executes(context -> {
                  PlayroomClient.kbd5Func = () -> ClientConfig.instance().value7 -= PlayroomClient.kbdInc;
                  PlayroomClient.kbd4Func = () -> ClientConfig.instance().value7 += PlayroomClient.kbdInc;
                  return 1;
              })
            )
          ).then(literal("target")
            .then(literal("value8")
              .executes(context -> {
                  PlayroomClient.kbd5Func = () -> ClientConfig.instance().value8 -= PlayroomClient.kbdInc;
                  PlayroomClient.kbd4Func = () -> ClientConfig.instance().value8 += PlayroomClient.kbdInc;
                  return 1;
              })
            )
          ).then(literal("target")
            .then(literal("value9")
              .executes(context -> {
                  PlayroomClient.kbd5Func = () -> ClientConfig.instance().value9 -= PlayroomClient.kbdInc;
                  PlayroomClient.kbd4Func = () -> ClientConfig.instance().value9 += PlayroomClient.kbdInc;
                  return 1;
              })
            )
          )
        );

        dispatcher.register(literal("pc")
          .then(literal("value1")
            .then(argument("value", FloatArgumentType.floatArg())
              .executes(context -> {
                  ClientConfig.instance().value1 = FloatArgumentType.getFloat(context, "value");
                  return 1;
              })
            )
          ).then(literal("value2")
            .then(argument("value", FloatArgumentType.floatArg())
              .executes(context -> {
                  ClientConfig.instance().value2 = FloatArgumentType.getFloat(context, "value");
                  return 1;
              })
            )
          ).then(literal("value3")
            .then(argument("value", FloatArgumentType.floatArg())
              .executes(context -> {
                  ClientConfig.instance().value3 = FloatArgumentType.getFloat(context, "value");
                  return 1;
              })
            )
          ).then(literal("value4")
            .then(argument("value", FloatArgumentType.floatArg())
              .executes(context -> {
                  ClientConfig.instance().value4 = FloatArgumentType.getFloat(context, "value");
                  return 1;
              })
            )
          ).then(literal("value5")
            .then(argument("value", FloatArgumentType.floatArg())
              .executes(context -> {
                  ClientConfig.instance().value5 = FloatArgumentType.getFloat(context, "value");
                  return 1;
              })
            )
          ).then(literal("value6")
            .then(argument("value", FloatArgumentType.floatArg())
              .executes(context -> {
                  ClientConfig.instance().value6 = FloatArgumentType.getFloat(context, "value");
                  return 1;
              })
            )
          ).then(literal("value7")
            .then(argument("value", FloatArgumentType.floatArg())
              .executes(context -> {
                  ClientConfig.instance().value7 = FloatArgumentType.getFloat(context, "value");
                  return 1;
              })
            )
          ).then(literal("value8")
            .then(argument("value", FloatArgumentType.floatArg())
              .executes(context -> {
                  ClientConfig.instance().value8 = FloatArgumentType.getFloat(context, "value");
                  return 1;
              })
            )
          ).then(literal("value9")
            .then(argument("value", FloatArgumentType.floatArg())
              .executes(context -> {
                  ClientConfig.instance().value9 = FloatArgumentType.getFloat(context, "value");
                  return 1;
              })
            )
          )
        );
    }
}
