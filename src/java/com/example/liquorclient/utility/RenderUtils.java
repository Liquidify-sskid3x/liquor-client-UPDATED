package com.example.liquorclient.utility;

import com.example.liquorclient.utility.CustomFontRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class RenderUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean isInside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    public static void drawOutline(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    public static void drawOutlinedBox(DrawContext ctx, int x, int y, int w, int h, int color) {
        drawOutline(ctx, x - 1, y - 1, w + 2, h + 2, 0x90000000);
        drawOutline(ctx, x + 1, y + 1, Math.max(1, w - 2), Math.max(1, h - 2), 0x90000000);
        drawOutline(ctx, x, y, w, h, color);
    }

    public static void drawScaledText(DrawContext ctx, String text, int x, int y, float scale, int color) {
        TextRenderer textRenderer = mc.textRenderer;
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(x, y);
        ctx.getMatrices().scale(scale, scale);
        CustomFontRenderer.drawString(ctx, textRenderer, text, 0, 0, color);
        ctx.getMatrices().popMatrix();
    }

    public static void drawCenteredText(DrawContext ctx, String text, int x, int y, int color) {
        TextRenderer textRenderer = mc.textRenderer;
        int textWidth = CustomFontRenderer.getStringWidth(textRenderer, text);
        CustomFontRenderer.drawString(ctx, textRenderer, text, x - textWidth / 2, y, color);
    }

    public static void drawGradient(DrawContext ctx, int x, int y, int w, int h, int color1, int color2) {
        ctx.fillGradient(x, y, x + w, y + h, color1, color2);
    }

    public static void drawVerticalGradient(DrawContext ctx, int x, int y, int w, int h, int top, int bottom) {
        ctx.fillGradient(x, y, x + w, y + h, top, bottom);
    }

    public static void drawHorizontalGradient(DrawContext ctx, int x, int y, int w, int h, int left, int right) {
        int segments = Math.max(2, w);
        for (int i = 0; i < segments; i++) {
            float ratio = (float) i / (segments - 1);
            int color = ColorUtils.blend(left, right, ratio);
            ctx.fill(x + i, y, x + i + 1, y + h, color);
        }
    }

    public static void drawRect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + h, color);
    }

    public static void drawRoundedRect(DrawContext ctx, int x, int y, int w, int h, int radius, int color) {
        ctx.fill(x + radius, y, x + w - radius, y + h, color);
        ctx.fill(x, y + radius, x + w, y + h - radius, color);
        drawCircle(ctx, x + radius, y + radius, radius, color);
        drawCircle(ctx, x + w - radius, y + radius, radius, color);
        drawCircle(ctx, x + radius, y + h - radius, radius, color);
        drawCircle(ctx, x + w - radius, y + h - radius, radius, color);
    }

    private static void drawCircle(DrawContext ctx, int cx, int cy, int r, int color) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                if (dx * dx + dy * dy <= r * r) {
                    ctx.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
                }
            }
        }
    }

    public static void drawButton(DrawContext ctx, int x, int y, int w, int h, String label, int bg, int outline, int text) {
        ctx.fill(x, y, x + w, y + h, bg);
        drawOutline(ctx, x, y, w, h, outline);
        drawCenteredText(ctx, label, x + w / 2, y + (h - CustomFontRenderer.getFontHeight()) / 2 + 1, text);
    }

    public static void drawHoverableButton(DrawContext ctx, int x, int y, int w, int h, String label, int accent, double mx, double my) {
        boolean hovered = isInside(mx, my, x, y, w, h);
        int bg = hovered ? 0xFF162131 : 0xFF101821;
        drawButton(ctx, x, y, w, h, label, bg, accent, 0xFFC4CAD3);
    }

    public static void drawTooltip(DrawContext ctx, String text, int mx, int my, int accent) {
        TextRenderer textRenderer = mc.textRenderer;
        int tw = CustomFontRenderer.getStringWidth(textRenderer, text);
        int tx = Math.min(mx + 8, mc.getWindow().getScaledWidth() - tw - 12);
        int ty = my - 12;
        int bw = tw + 8;
        int totalH = CustomFontRenderer.getFontHeight() + 6;
        ctx.fill(tx, ty, tx + bw, ty + totalH, 0xCC0B1017);
        drawOutline(ctx, tx, ty, bw, totalH, accent);
        CustomFontRenderer.drawString(ctx, textRenderer, text, tx + 4, ty + 3, 0xFFFFFFFF);
    }

    public static void drawScrollbar(DrawContext ctx, int x, int y, int h, double scroll, double maxScroll, int accent) {
        int barH = (int) (h * Math.min(1.0, h / (h + maxScroll)));
        int barY = y + (int) ((scroll / Math.max(1, maxScroll)) * (h - barH));
        ctx.fill(x, y, x + 2, y + h, 0xFF1B2430);
        ctx.fill(x, barY, x + 2, barY + barH, accent);
    }
}
