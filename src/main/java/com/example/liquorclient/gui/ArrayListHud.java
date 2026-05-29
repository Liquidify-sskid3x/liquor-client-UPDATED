package com.example.liquorclient.gui;

import com.example.liquorclient.config.ConfigManager;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.ModuleManager;
import com.example.liquorclient.render.ClickGuiMod;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrayListHud {
    private static final Map<Module, EntryAnimation> animations = new HashMap<>();
    private static boolean initialized = false;
    private static long lastFrameTime = System.currentTimeMillis();
    private static int infoX = 8;
    private static int infoY = 8;
    private static int arrayX = -1;
    private static int arrayY = 8;
    private static int armorX = 8;
    private static int armorY = 42;
    private static int potionX = 8;
    private static int potionY = 124;
    private static int keystrokesX = 8;
    private static int keystrokesY = 206;
    private static int cpsX = 8;
    private static int cpsY = 290;

    public static void init() {
        if (initialized) return;
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> render(drawContext));
        initialized = true;
    }

    private static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;

        long now = System.currentTimeMillis();
        float delta = Math.min(0.05f, (now - lastFrameTime) / 1000.0f);
        lastFrameTime = now;

        renderInfoHud(ctx, mc);

        int screenW = mc.getWindow().getScaledWidth();
        int y = arrayY;
        int rowH = 12;
        int accent = ClickGuiMod.accentColor(now);

        if (!ClickGuiMod.shouldShowArrayList()) return;

        Comparator<Module> comparator = ClickGuiMod.shouldSortArrayListByWidth()
                ? Comparator.comparingInt((Module module) -> mc.textRenderer.getWidth(module.getName())).reversed()
                : Comparator.comparing(Module::getName);
        List<Module> modules = ModuleManager.getModules().stream()
                .sorted(comparator)
                .toList();

        boolean rightAligned = arrayX < 0 || arrayX > screenW / 2;

        for (Module module : modules) {
            EntryAnimation anim = animations.computeIfAbsent(module, ignored -> new EntryAnimation());
            anim.update(module.isEnabled(), delta);
            if (anim.isGone()) continue;

            int textW = mc.textRenderer.getWidth(module.getName());
            int drawX, rowLeft, rowRight, accentLeft, accentRight;

            if (rightAligned) {
                // Anchored to the right: text ends at the right edge of the bounding box
                int maxW = getMaxArrayWidth();
                int endX = arrayX < 0 ? screenW - 8 : arrayX + maxW;
                drawX = Math.round(endX - textW + (1.0f - anim.slide) * (textW + 18));
                rowLeft = drawX - 5;
                rowRight = endX;
                accentLeft = rowRight - 2;
                accentRight = rowRight;
            } else {
                // Anchored to the left: text starts at arrayX
                int startX = arrayX < 0 ? 8 : arrayX;
                drawX = Math.round(startX - (1.0f - anim.slide) * (textW + 18));
                rowLeft = startX - 5;
                rowRight = drawX + textW + 5;
                accentLeft = rowLeft;
                accentRight = rowLeft + 2;
            }

            int drawY = Math.round(y + (1.0f - anim.space) * rowH * 0.65f);
            int alpha = Math.round(255.0f * anim.alpha);
            int textColor = (alpha << 24) | 0x00FFFFFF;
            int bgColor = (Math.round(110.0f * anim.alpha) << 24);

            ctx.fill(rowLeft, drawY - 2, rowRight, drawY + 10, bgColor);
            ctx.fill(accentLeft, drawY - 2, accentRight, drawY + 10, withAlpha(accent, alpha));
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal(module.getName()), drawX, drawY, textColor);

            y += Math.round(rowH * anim.space);
        }

        animations.entrySet().removeIf(entry -> entry.getValue().isGone() && !entry.getKey().isEnabled());
    }

    private static void renderInfoHud(DrawContext ctx, MinecraftClient mc) {
        int x = infoX;
        int y = infoY;
        int color = 0xFFFFFFFF;
        int muted = 0xFFB6C0CD;

        if (ClickGuiMod.shouldShowFps()) {
            String text = "FPS: " + mc.getCurrentFps();
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal(text), x, y, color);
            y += 11;
        }

        if (ClickGuiMod.shouldShowCoordinates()) {
            String text = "XYZ: " + Math.floor(mc.player.getX()) + ", " + Math.floor(mc.player.getY()) + ", " + Math.floor(mc.player.getZ());
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal(text), x, y, color);
            y += 11;
        }

        if (ClickGuiMod.shouldShowServerIp()) {
            ServerInfo info = mc.getCurrentServerEntry();
            String address = info == null ? "Singleplayer" : info.address;
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal("Server: "), x, y, muted);
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal(address), x + mc.textRenderer.getWidth("Server: "), y, color);
        }
    }

    public static int getInfoX() {
        return infoX;
    }

    public static int getInfoY() {
        return infoY;
    }

    public static int getArrayX() {
        return arrayX;
    }

    public static int getArrayY() {
        return arrayY;
    }

    public static void setInfoPosition(int x, int y) {
        setInfoPositionSilently(x, y);
        ConfigManager.requestSave();
    }

    public static void setInfoPositionSilently(int x, int y) {
        infoX = x;
        infoY = y;
    }

    public static void setArrayPosition(int x, int y) {
        setArrayPositionSilently(x, y);
        ConfigManager.requestSave();
    }

    public static void setArrayPositionSilently(int x, int y) {
        arrayX = x;
        arrayY = y;
    }

    public static int getArmorX() { return armorX; }
    public static int getArmorY() { return armorY; }
    public static void setArmorPosition(int x, int y) {
        armorX = x;
        armorY = y;
        ConfigManager.requestSave();
    }

    public static int getPotionX() { return potionX; }
    public static int getPotionY() { return potionY; }
    public static void setPotionPosition(int x, int y) {
        potionX = x;
        potionY = y;
        ConfigManager.requestSave();
    }

    public static int getKeystrokesX() { return keystrokesX; }
    public static int getKeystrokesY() { return keystrokesY; }
    public static void setKeystrokesPosition(int x, int y) {
        keystrokesX = x;
        keystrokesY = y;
        ConfigManager.requestSave();
    }

    public static int getCpsX() { return cpsX; }
    public static int getCpsY() { return cpsY; }
    public static void setCpsPosition(int x, int y) {
        cpsX = x;
        cpsY = y;
        ConfigManager.requestSave();
    }

    public static void setPositionsSilently(int ax, int ay, int px, int py, int kx, int ky, int cx, int cy) {
        armorX = ax; armorY = ay;
        potionX = px; potionY = py;
        keystrokesX = kx; keystrokesY = ky;
        cpsX = cx; cpsY = cy;
    }

    public static void resetPositions() {
        infoX = 8;
        infoY = 8;
        arrayX = -1;
        arrayY = 8;
        armorX = 8;
        armorY = 42;
        potionX = 8;
        potionY = 124;
        keystrokesX = 8;
        keystrokesY = 206;
        cpsX = 8;
        cpsY = 290;
        ConfigManager.requestSave();
    }

    public static int getArrayDrawX(int screenWidth, int textWidth) {
        if (arrayX < 0) return screenWidth - textWidth - 8;
        if (arrayX > screenWidth / 2) return arrayX; // Anchored to its X
        return arrayX;
    }

    public static int getMaxArrayWidth() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return ModuleManager.getModules().stream()
                .filter(Module::isEnabled)
                .mapToInt(m -> mc.textRenderer.getWidth(m.getName()))
                .max()
                .orElse(100) + 10;
    }

    public static int getArrayHeight() {
        int enabled = (int) ModuleManager.getModules().stream().filter(Module::isEnabled).count();
        return Math.max(12, enabled * 12);
    }

    public static int getInfoWidth() {
        return 120;
    }

    public static int getInfoHeight() {
        return 33;
    }

    public static int getArmorWidth() { return 100; }
    public static int getArmorHeight() { return 80; }

    public static int getPotionWidth() { return 100; }
    public static int getPotionHeight() { return 60; }

    public static int getKeystrokesWidth() { return 70; }
    public static int getKeystrokesHeight() { return 80; }

    public static int getCpsWidth() { return 120; }
    public static int getCpsHeight() { return 12; }

    private static int withAlpha(int color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
    }

    private static class EntryAnimation {
        private float space = 0.0f;
        private float slide = 0.0f;
        private float alpha = 0.0f;

        private void update(boolean enabled, float delta) {
            float spaceTarget = enabled ? 1.0f : 0.0f;
            float visualTarget = enabled ? 1.0f : 0.0f;

            space = approach(space, spaceTarget, delta * 8.5f);
            slide = approach(slide, visualTarget, delta * 13.0f);
            alpha = approach(alpha, visualTarget, delta * 11.0f);
        }

        private boolean isGone() {
            return space <= 0.01f && slide <= 0.01f && alpha <= 0.01f;
        }

        private float approach(float current, float target, float amount) {
            if (current < target) {
                return Math.min(target, current + amount);
            }
            return Math.max(target, current - amount);
        }
    }
}
