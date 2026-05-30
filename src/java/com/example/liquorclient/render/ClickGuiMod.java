package com.example.liquorclient.render;

import com.example.liquorclient.module.BooleanSetting;
import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.ColorSetting;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.ModuleManager;
import com.example.liquorclient.module.NumberSetting;

public class ClickGuiMod extends Module {
    private final BooleanSetting rainbowAccent = new BooleanSetting("Rainbow Accent", true);
    private final ColorSetting accentColor = new ColorSetting("Accent Color", 0xFFFFBE18);
    private final BooleanSetting arrayList = new BooleanSetting("ArrayList", true);
    private final BooleanSetting arrayListWidthSort = new BooleanSetting("ArrayList Width Sort", true);
    private final BooleanSetting fpsHud = new BooleanSetting("FPS HUD", true);
    private final BooleanSetting coordinatesHud = new BooleanSetting("Coordinates HUD", true);
    private final BooleanSetting serverIpHud = new BooleanSetting("Server IP HUD", true);
    private final NumberSetting fontFace = new NumberSetting("Font Face", 1.0, 0.0, 3.0, 1.0);
    private final NumberSetting fontSize = new NumberSetting("Font Size", 18.0, 10.0, 30.0, 1.0);

    public ClickGuiMod() {
        super("ClickGUI", Category.RENDER, "Opens the ClickGUI to manage modules");
        addSetting(rainbowAccent);
        addSetting(accentColor);
        addSetting(arrayList);
        addSetting(arrayListWidthSort);
        addSetting(fpsHud);
        addSetting(coordinatesHud);
        addSetting(serverIpHud);
        addSetting(fontFace);
        addSetting(fontSize);
    }

    public static ClickGuiMod get() {
        return ModuleManager.getModule(ClickGuiMod.class);
    }

    public static int accentColor(long now) {
        ClickGuiMod module = get();
        if (module == null || module.rainbowAccent.getValue()) {
            float hue = (now % 2500L) / 2500.0f;
            return java.awt.Color.HSBtoRGB(hue, 0.9f, 1.0f) | 0xFF000000;
        }

        return module.accentColor.getValue();
    }

    public static boolean shouldShowArrayList() {
        ClickGuiMod module = get();
        return module == null || module.arrayList.getValue();
    }

    public static boolean shouldSortArrayListByWidth() {
        ClickGuiMod module = get();
        return module == null || module.arrayListWidthSort.getValue();
    }

    public static boolean shouldShowFps() {
        ClickGuiMod module = get();
        return module != null && module.fpsHud.getValue();
    }

    public static boolean shouldShowCoordinates() {
        ClickGuiMod module = get();
        return module != null && module.coordinatesHud.getValue();
    }

    public static boolean shouldShowServerIp() {
        ClickGuiMod module = get();
        return module != null && module.serverIpHud.getValue();
    }

    public double getFontFace() {
        return fontFace.getValue();
    }

    public void setFontFace(double value) {
        fontFace.setValue(value);
    }

    public double getFontSize() {
        return fontSize.getValue();
    }

    public void setFontSize(double value) {
        fontSize.setValue(value);
    }

    @Override
    protected void onEnable() {
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc != null && mc.mouse != null && mc.currentScreen == null) {
            mc.setScreen(new com.example.liquorclient.gui.ClickGui());
        }
    }

    @Override
    protected void onDisable() {
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc != null && mc.currentScreen instanceof com.example.liquorclient.gui.ClickGui) {
            mc.setScreen(null);
        }
    }
}
