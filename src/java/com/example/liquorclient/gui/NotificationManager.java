package com.example.liquorclient.gui;

import com.example.liquorclient.render.ClickGuiMod;
import com.example.liquorclient.utility.CustomFontRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public final class NotificationManager {
    private static final List<Toast> TOASTS = new ArrayList<>();
    private static final long LIFE_MS = 3000L;
    private static final long ANIM_MS = 300L;
    private static boolean initialized = false;

    private NotificationManager() {}

    public static void init() {
        if (initialized) return;
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> render(drawContext));
        initialized = true;
    }

    public static void push(String title, String message) {
        TOASTS.add(new Toast(title, message, System.currentTimeMillis()));
    }

    private static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden || TOASTS.isEmpty()) return;

        long now = System.currentTimeMillis();
        int screenW = ctx.getScaledWindowWidth();
        int screenH = ctx.getScaledWindowHeight();
        int accent = ClickGuiMod.accentColor(now);
        
        float currentY = screenH - 40;

        for (int i = 0; i < TOASTS.size(); i++) {
            Toast toast = TOASTS.get(i);
            long age = now - toast.createdAt;

            if (age > LIFE_MS) {
                TOASTS.remove(i--);
                continue;
            }

            // Slide in/out animation
            float xOffset = 0;
            if (age < ANIM_MS) {
                xOffset = (1.0f - (float) age / ANIM_MS) * 200;
            } else if (age > LIFE_MS - ANIM_MS) {
                xOffset = ((float) (age - (LIFE_MS - ANIM_MS)) / ANIM_MS) * 200;
            }

            // Target Y for stacking
            toast.targetY = currentY;
            if (toast.currentY == 0) toast.currentY = toast.targetY + 40;
            
            // Smooth vertical movement
            toast.currentY = MathHelper.lerp(0.1f, toast.currentY, toast.targetY);

            int titleW = CustomFontRenderer.getStringWidth(mc.textRenderer, toast.title);
            int messageW = CustomFontRenderer.getStringWidth(mc.textRenderer, toast.message);
            int width = Math.max(120, Math.max(titleW, messageW) + 20);
            float drawX = screenW - width - 10 + xOffset;
            float drawY = toast.currentY;

            int bg = 0xEE0B1017;
            int text = 0xFFFFFFFF;
            int muted = 0xFFAAB4C2;

            ctx.fill((int)drawX, (int)drawY, (int)drawX + width, (int)drawY + 30, bg);
            ctx.fill((int)drawX, (int)drawY, (int)drawX + 2, (int)drawY + 30, accent);
            
            CustomFontRenderer.drawString(ctx, mc.textRenderer, toast.title, (int)drawX + 8, (int)drawY + 5, text);
            CustomFontRenderer.drawString(ctx, mc.textRenderer, toast.message, (int)drawX + 8, (int)drawY + 17, muted);

            currentY -= 35;
        }
    }

    private static class Toast {
        String title, message;
        long createdAt;
        float currentY, targetY;

        Toast(String title, String message, long createdAt) {
            this.title = title;
            this.message = message;
            this.createdAt = createdAt;
        }
    }
}
