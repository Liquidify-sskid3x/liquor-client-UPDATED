package com.example.liquorclient.gui;

import com.example.liquorclient.config.ConfigManager;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.ModuleManager;
import com.example.liquorclient.render.ClickGuiMod;
import com.example.liquorclient.utility.CustomFontRenderer;
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
    public static final int ANCHOR_ORIGINAL = 0;
    public static final int ANCHOR_LEFT = 1;
    public static final int ANCHOR_DOWN = 2;
    public static final int ANCHOR_RIGHT = 3;

    private static final Map<Module, EntryAnimation> animations = new HashMap<>();
    private static boolean initialized = false;
    private static long lastFrameTime = System.currentTimeMillis();
    private static int infoX = 8;
    private static int infoY = 8;
    private static int infoAnchor = ANCHOR_ORIGINAL;
    private static int arrayX = -1;
    private static int arrayY = 8;
    private static int arrayAnchor = ANCHOR_ORIGINAL;
    private static int armorX = 8;
    private static int armorY = 42;
    private static int armorAnchor = ANCHOR_ORIGINAL;
    private static int potionX = 8;
    private static int potionY = 124;
    private static int potionAnchor = ANCHOR_ORIGINAL;
    private static int keystrokesX = 8;
    private static int keystrokesY = 206;
    private static int keystrokesAnchor = ANCHOR_ORIGINAL;
    private static int cpsX = 8;
    private static int cpsY = 290;
    private static int cpsAnchor = ANCHOR_ORIGINAL;

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
        int screenH = mc.getWindow().getScaledHeight();
        int y = arrayY;
        int rowH = 12;
        int accent = ClickGuiMod.accentColor(now);

        if (!ClickGuiMod.shouldShowArrayList()) return;

        int anchor = getArrayAnchor() != ANCHOR_ORIGINAL ? getArrayAnchor()
                : (arrayX < 0 || arrayX > screenW / 2 ? ANCHOR_RIGHT : ANCHOR_LEFT);

        Comparator<Module> comparator = ClickGuiMod.shouldSortArrayListByWidth()
                ? Comparator.comparingInt((Module module) -> CustomFontRenderer.getStringWidth(mc.textRenderer, module.getName())).reversed()
                : Comparator.comparing(Module::getName);
        List<Module> modules = ModuleManager.getModules().stream()
                .sorted(comparator)
                .toList();

        for (Module module : modules) {
            EntryAnimation anim = animations.computeIfAbsent(module, ignored -> new EntryAnimation());
            anim.update(module.isEnabled(), delta);
            if (anim.isGone()) continue;

            int textW = CustomFontRenderer.getStringWidth(mc.textRenderer, module.getName());
            int accentW = 2;
            int gap = 6;
            int margin = 5;
            int drawX, rowLeft, rowRight, accentLeft, accentRight;

            switch (anchor) {
                case ANCHOR_RIGHT -> {
                    // Accent at right edge, text to its left
                    int endX = screenW - 8;
                    accentLeft = endX - accentW;
                    accentRight = endX;
                    drawX = Math.round(endX - accentW - gap - textW + (1.0f - anim.slide) * (textW + 18));
                    rowLeft = drawX - margin;
                    rowRight = endX;
                }
                case ANCHOR_LEFT -> {
                    // Accent at left edge, text to its right
                    int startX = 8;
                    accentLeft = startX;
                    accentRight = startX + accentW;
                    drawX = Math.round(startX + accentW + gap - (1.0f - anim.slide) * (textW + 18));
                    rowLeft = startX;
                    rowRight = drawX + textW + margin;
                }
                case ANCHOR_DOWN -> {
                    // Accent at right edge, text to its left (same as RIGHT visually)
                    int endX = screenW - 8;
                    int h = getArrayHeight();
                    int baseY = screenH - 8 - h;
                    y = baseY;
                    accentLeft = endX - accentW;
                    accentRight = endX;
                    drawX = Math.round(endX - accentW - gap - textW + (1.0f - anim.slide) * (textW + 18));
                    rowLeft = drawX - margin;
                    rowRight = endX;
                }
                default -> {
                    int endX = screenW - 8;
                    accentLeft = endX - accentW;
                    accentRight = endX;
                    drawX = Math.round(endX - accentW - gap - textW + (1.0f - anim.slide) * (textW + 18));
                    rowLeft = drawX - margin;
                    rowRight = endX;
                }
            }

            int drawY = Math.round(y + (1.0f - anim.space) * rowH * 0.65f);
            int alpha = Math.round(255.0f * anim.alpha);
            int textColor = (alpha << 24) | 0x00FFFFFF;
            int bgColor = (Math.round(110.0f * anim.alpha) << 24);

            ctx.fill(rowLeft, drawY - 2, rowRight, drawY + 10, bgColor);
            ctx.fill(accentLeft, drawY - 2, accentRight, drawY + 10, withAlpha(accent, alpha));
            CustomFontRenderer.drawString(ctx, mc.textRenderer, module.getName(), drawX, drawY, textColor);

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
            CustomFontRenderer.drawString(ctx, mc.textRenderer, text, x, y, color);
            y += 11;
        }

        if (ClickGuiMod.shouldShowCoordinates()) {
            String text = "XYZ: " + Math.floor(mc.player.getX()) + ", " + Math.floor(mc.player.getY()) + ", " + Math.floor(mc.player.getZ());
            CustomFontRenderer.drawString(ctx, mc.textRenderer, text, x, y, color);
            y += 11;
        }

        if (ClickGuiMod.shouldShowServerIp()) {
            ServerInfo info = mc.getCurrentServerEntry();
            String address = info == null ? "Singleplayer" : info.address;
            CustomFontRenderer.drawString(ctx, mc.textRenderer, "Server: ", x, y, muted);
            CustomFontRenderer.drawString(ctx, mc.textRenderer, address, x + CustomFontRenderer.getStringWidth(mc.textRenderer, "Server: "), y, color);
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
        infoAnchor = ANCHOR_ORIGINAL;
        arrayAnchor = ANCHOR_ORIGINAL;
        armorAnchor = ANCHOR_ORIGINAL;
        potionAnchor = ANCHOR_ORIGINAL;
        keystrokesAnchor = ANCHOR_ORIGINAL;
        cpsAnchor = ANCHOR_ORIGINAL;
        ConfigManager.requestSave();
    }

    public static int getArrayDrawX(int screenWidth, int textWidth) {
        if (arrayX < 0) return screenWidth - textWidth - 8;
        if (arrayX > screenWidth / 2) return arrayX;
        return arrayX;
    }

    public static int getMaxArrayWidth() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return ModuleManager.getModules().stream()
                .filter(Module::isEnabled)
                .mapToInt(m -> CustomFontRenderer.getStringWidth(mc.textRenderer, m.getName()))
                .max()
                .orElse(100) + 20;
    }

    public static int getArrayHeight() {
        int enabled = (int) ModuleManager.getModules().stream().filter(Module::isEnabled).count();
        return Math.max(12, enabled * 12);
    }

    public static int getInfoWidth() { return 120; }
    public static int getInfoHeight() { return 33; }
    public static int getArmorWidth() { return 100; }
    public static int getArmorHeight() { return 80; }
    public static int getPotionWidth() { return 100; }
    public static int getPotionHeight() { return 60; }
    public static int getKeystrokesWidth() { return 70; }
    public static int getKeystrokesHeight() { return 80; }
    public static int getCpsWidth() { return 120; }
    public static int getCpsHeight() { return 12; }

    // Anchor getters
    public static int getInfoAnchor() { return infoAnchor; }
    public static int getArrayAnchor() { return arrayAnchor; }
    public static int getArmorAnchor() { return armorAnchor; }
    public static int getPotionAnchor() { return potionAnchor; }
    public static int getKeystrokesAnchor() { return keystrokesAnchor; }
    public static int getCpsAnchor() { return cpsAnchor; }

    // Anchor setters (with save)
    public static void setInfoAnchor(int a) { infoAnchor = a % 4; ConfigManager.requestSave(); }
    public static void setArrayAnchor(int a) { arrayAnchor = a % 4; ConfigManager.requestSave(); }
    public static void setArmorAnchor(int a) { armorAnchor = a % 4; ConfigManager.requestSave(); }
    public static void setPotionAnchor(int a) { potionAnchor = a % 4; ConfigManager.requestSave(); }
    public static void setKeystrokesAnchor(int a) { keystrokesAnchor = a % 4; ConfigManager.requestSave(); }
    public static void setCpsAnchor(int a) { cpsAnchor = a % 4; ConfigManager.requestSave(); }
    // Silent anchor setters (no save)
    public static void setInfoAnchorSilently(int a) { infoAnchor = a % 4; }
    public static void setArrayAnchorSilently(int a) { arrayAnchor = a % 4; }
    public static void setArmorAnchorSilently(int a) { armorAnchor = a % 4; }
    public static void setPotionAnchorSilently(int a) { potionAnchor = a % 4; }
    public static void setKeystrokesAnchorSilently(int a) { keystrokesAnchor = a % 4; }
    public static void setCpsAnchorSilently(int a) { cpsAnchor = a % 4; }

    public static int cycleAnchor(int current) {
        return (current + 1) % 4;
    }

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
