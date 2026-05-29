package com.example.liquorclient.module;

import org.lwjgl.glfw.GLFW;

public class KeybindSetting extends Setting<Integer> {
    public KeybindSetting() {
        super("Keybind", GLFW.GLFW_KEY_UNKNOWN);
    }
}
