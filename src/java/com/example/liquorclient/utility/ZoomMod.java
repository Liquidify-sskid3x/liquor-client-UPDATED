package com.example.liquorclient.utility;

import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.NumberSetting;
import com.example.liquorclient.module.ModuleManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class ZoomMod extends Module {
    public final NumberSetting factor = new NumberSetting("Factor", 3.0, 1.0, 10.0, 0.1);
    private static double currentFactor = 1.0;
    private static double lastFactor = 1.0;

    public ZoomMod() {
        super("Zoom", Category.RENDER, "Zooms your view in and out");
        addSetting(factor);
    }

    @Override
    public void onEnable() {
        // We don't want to reset factors here to keep it smooth if toggled quickly
    }

    public static float apply(float fov) {
        ZoomMod zoom = ModuleManager.getModule(ZoomMod.class);
        if (zoom == null) return fov;

        float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(true);
        double zoomFactor = MathHelper.lerp(tickDelta, lastFactor, currentFactor);

        return (float) (fov / zoomFactor);
    }

    public static void tick() {
        ZoomMod zoom = ModuleManager.getModule(ZoomMod.class);
        if (zoom == null) return;

        lastFactor = currentFactor;
        
        double target = 1.0;
        if (zoom.isActive()) {
            target = zoom.factor.getValue();
        }

        currentFactor = MathHelper.lerp(0.25, currentFactor, target);
        if (Math.abs(currentFactor - target) < 0.001) {
            currentFactor = target;
        }
    }

    public boolean isActive() {
        if (isEnabled()) return true;
        
        int key = getKeybind().getValue();
        if (key != GLFW.GLFW_KEY_UNKNOWN) {
            long window = MinecraftClient.getInstance().getWindow().getHandle();
            return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
        }
        
        return false;
    }

    public static boolean onScroll(double amount) {
        ZoomMod zoom = ModuleManager.getModule(ZoomMod.class);
        if (zoom != null && zoom.isActive()) {
            double newVal = zoom.factor.getValue() + amount * 0.25;
            zoom.factor.setValue(MathHelper.clamp(newVal, 1.0, 10.0));
            return true;
        }
        return false;
    }
}
