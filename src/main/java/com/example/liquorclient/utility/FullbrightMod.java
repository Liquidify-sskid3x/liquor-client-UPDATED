package com.example.liquorclient.utility;

import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class FullbrightMod extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public FullbrightMod() {
        super("Fullbright", Category.RENDER, "Gives you full brightness in dark areas");
    }

    public static boolean isFullbrightEnabled() {
        Module mod = com.example.liquorclient.module.ModuleManager.getModule(FullbrightMod.class);
        return mod != null && mod.isEnabled();
    }
}
