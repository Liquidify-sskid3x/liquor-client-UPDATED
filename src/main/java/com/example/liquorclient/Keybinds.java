package com.example.liquorclient;

import com.example.liquorclient.gui.ClickGui;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.ModuleManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class Keybinds {
    private static KeyBinding openMenuKey;
    private static final Map<Integer, Boolean> keyStates = new HashMap<>();

    public static void register() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.liquorclient.open_menu",
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                com.example.liquorclient.render.ClickGuiMod mod = com.example.liquorclient.render.ClickGuiMod.get();
                if (mod != null) {
                    mod.toggle();
                }
            }

            if (client.currentScreen != null) return;

            long window = client.getWindow().getHandle();

            java.util.Map<Integer, Boolean> newKeyStates = new java.util.HashMap<>();
            java.util.Map<Integer, Boolean> newlyPressedKeys = new java.util.HashMap<>();

            for (Module m : ModuleManager.getModules()) {
                int key = m.getKeybind().getValue();
                if (key != GLFW.GLFW_KEY_UNKNOWN) {
                    if (!newKeyStates.containsKey(key)) {
                        boolean isDown = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
                        boolean wasDown = keyStates.getOrDefault(key, false);
                        newKeyStates.put(key, isDown);
                        newlyPressedKeys.put(key, isDown && !wasDown);
                    }
                }
            }

            for (Module m : ModuleManager.getModules()) {
                int key = m.getKeybind().getValue();
                if (key != GLFW.GLFW_KEY_UNKNOWN && newlyPressedKeys.getOrDefault(key, false)) {
                    m.toggle();
                }
            }

            keyStates.putAll(newKeyStates);
        });
    }
}