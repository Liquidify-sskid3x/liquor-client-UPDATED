package com.example.liquorclient.gui;

import com.example.liquorclient.render.ClickGuiMod;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class NotificationManager {
    private static final List<Toast> TOASTS = new ArrayList<>();
    private static final long LIFE_MS = 2400L;
    private static boolean initialized = false;

    private NotificationManager() {
    }

    public static void init() {
        if (initialized) return;
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> render(drawContext));
        initialized = true;
    }

    public static void push(String title, String message) {
        TOASTS.add(new Toast(title, message, System.currentTimeMillis()));
        while (TOASTS.size() > 5) {
            TOASTS.remove(0);
        }
    }

    private static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden || TOASTS.isEmpty()) return;

        long now = System.currentTimeMillis();
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();
        int accent = ClickGuiMod.accentColor(now);
        int y = screenH - 34;

        Iterator<Toast> iterator = TOASTS.iterator();
        while (iterator.hasNext()) {
            Toast toast = iterator.next();
            long age = now - toast.createdAt;
            if (age > LIFE_MS) {
                iterator.remove();
                continue;
            }

            float progress = age / (float) LIFE_MS;
            float alpha = progress < 0.82f ? 1.0f : 1.0f - ((progress - 0.82f) / 0.18f);
            alpha = Math.max(0.0f, Math.min(1.0f, alpha));

            int titleW = mc.textRenderer.getWidth(toast.title);
            int messageW = mc.textRenderer.getWidth(toast.message);
            int width = Math.max(112, Math.max(titleW, messageW) + 20);
            int x = screenW - width - 8;
            int bg = (Math.round(190.0f * alpha) << 24) | 0x000B1017;
            int text = (Math.round(255.0f * alpha) << 24) | 0x00FFFFFF;
            int muted = (Math.round(210.0f * alpha) << 24) | 0x00AAB4C2;

            ctx.fill(x, y, x + width, y + 28, bg);
            ctx.fill(x, y, x + 2, y + 28, withAlpha(accent, Math.round(255.0f * alpha)));
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal(toast.title), x + 8, y + 5, text);
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal(toast.message), x + 8, y + 16, muted);

            y -= 32;
        }
    }

    private static int withAlpha(int color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
    }

    private record Toast(String title, String message, long createdAt) {
    }
}
