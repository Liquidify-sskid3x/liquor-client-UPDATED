package com.example.liquorclient.utility;

import com.example.liquorclient.gui.ArrayListHud;
import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;

public class KeystrokesMod extends Module {
    public KeystrokesMod() {
        super("Keystrokes", Category.RENDER, "Displays your key presses on screen");

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> render(ctx));
    }

    private void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!isEnabled() || mc.player == null || mc.options.hudHidden) return;

        int drawX = ArrayListHud.getKeystrokesX();
        int drawY = ArrayListHud.getKeystrokesY();
        drawKey(ctx, mc, "W", mc.options.forwardKey, drawX + 24, drawY, 22, 18);
        drawKey(ctx, mc, "A", mc.options.leftKey, drawX, drawY + 20, 22, 18);
        drawKey(ctx, mc, "S", mc.options.backKey, drawX + 24, drawY + 20, 22, 18);
        drawKey(ctx, mc, "D", mc.options.rightKey, drawX + 48, drawY + 20, 22, 18);
        drawKey(ctx, mc, "LMB", mc.options.attackKey, drawX, drawY + 42, 34, 18);
        drawKey(ctx, mc, "RMB", mc.options.useKey, drawX + 36, drawY + 42, 34, 18);
        drawKey(ctx, mc, "SPACE", mc.options.jumpKey, drawX, drawY + 64, 70, 14);
    }

    private void drawKey(DrawContext ctx, MinecraftClient mc, String label, KeyBinding key, int x, int y, int w, int h) {
        boolean down = key.isPressed();
        ctx.fill(x, y, x + w, y + h, down ? 0xCCFFFFFF : 0x99000000);
        drawOutline(ctx, x, y, w, h, down ? 0xFFFFFFFF : 0xFF263241);
        int color = down ? 0xFF111111 : 0xFFFFFFFF;
        int labelW = mc.textRenderer.getWidth(label);
        ctx.drawTextWithShadow(mc.textRenderer, Text.literal(label), x + w / 2 - labelW / 2, y + h / 2 - 4, color);
    }

    private void drawOutline(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }
}
