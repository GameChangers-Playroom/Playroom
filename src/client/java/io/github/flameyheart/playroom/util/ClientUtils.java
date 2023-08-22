package io.github.flameyheart.playroom.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ClientUtils {
    public static KeyBinding addKeybind(String name, InputUtil.Type type, int code) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding("key.playroom." + name, type, code, "category.playroom.keybinds"));
    }

    public static KeyBinding addKeybind(String name, int code) {
        return addKeybind(name, InputUtil.Type.KEYSYM, code);
    }

    public static void listenKeybind(KeyBinding keybind, Consumer<MinecraftClient> action) {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keybind.wasPressed()) {
                action.accept(client);
            }
        });
    }
}
