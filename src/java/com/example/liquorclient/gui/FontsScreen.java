package com.example.liquorclient.gui;

import com.example.liquorclient.render.ClickGuiMod;
import com.example.liquorclient.utility.CustomFontRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class FontsScreen extends Screen {
    private static final int PANEL_W = 440;
    private static final int PANEL_H = 300;
    private static final String PREVIEW_TEXT = "The Quick Brown Fox Jumped Over The Lazy Dog";
    private static final String[] FONT_LABELS = { "Default", "Orbitron", "Chakra Petch", "Poppins" };

    private final Screen parent;
    private int selected;

    public FontsScreen(Screen parent) {
        super(Text.literal("Fonts"));
        this.parent = parent;
        ClickGuiMod mod = ClickGuiMod.get();
        selected = (int) (mod != null ? mod.getFontFace() : 0);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int accent = ClickGuiMod.accentColor(System.currentTimeMillis());
        int x = width / 2 - PANEL_W / 2;
        int y = height / 2 - PANEL_H / 2;

        ctx.fill(0, 0, width, height, 0x88000000);
        ctx.fill(x, y, x + PANEL_W, y + PANEL_H, 0xFF0B1017);
        ctx.fill(x, y, x + PANEL_W, y + 2, accent);

        CustomFontRenderer.drawString(ctx, textRenderer, "Fonts", x + 12, y + 12, 0xFFFFFFFF);

        int previewY = y + 46;
        int previewH = 60;
        ctx.fill(x + 12, previewY, x + PANEL_W - 12, previewY + previewH, 0xFF070A0F);
        drawOutline(ctx, x + 12, previewY, PANEL_W - 24, previewH, 0xFF263241);

        Text previewText = CustomFontRenderer.withFont(PREVIEW_TEXT, selected);
        int pw = textRenderer.getWidth(previewText);
        ctx.drawTextWithShadow(textRenderer, previewText, x + PANEL_W / 2 - pw / 2, previewY + 26, 0xFFFFFFFF);

        int labelY = previewY + previewH + 14;
        CustomFontRenderer.drawString(ctx, textRenderer, "Select a font:", x + 12, labelY, 0xFF9BA8B7);

        int fontY = labelY + 24;
        int fontH = 34;
        for (int i = 0; i < FONT_LABELS.length; i++) {
            boolean hovered = isInside(mouseX, mouseY, x + 12, fontY, PANEL_W - 24, fontH);
            boolean active = i == selected;

            ctx.fill(x + 12, fontY, x + PANEL_W - 12, fontY + fontH, active ? 0xFF132014 : hovered ? 0xFF182231 : 0xFF0F151D);
            ctx.fill(x + 12, fontY, x + 14, fontY + fontH, active ? accent : 0xFF263241);

            Text fontText = CustomFontRenderer.withFont(FONT_LABELS[i], i);
            int fw = textRenderer.getWidth(fontText);
            ctx.drawTextWithShadow(textRenderer, fontText, x + PANEL_W / 2 - fw / 2, fontY + 12, active ? 0xFFE9FFE6 : 0xFFC4CAD3);

            fontY += fontH + 4;
        }

        drawButton(ctx, x + 12, y + PANEL_H - 30, 70, 20, "Back", mouseX, mouseY);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return super.mouseClicked(click, doubled);

        int x = width / 2 - PANEL_W / 2;
        int y = height / 2 - PANEL_H / 2;

        if (isInside(mouseX, mouseY, x + 12, y + PANEL_H - 30, 70, 20)) {
            close();
            return true;
        }

        int previewY = y + 46;
        int labelY = previewY + 60 + 14;
        int fontY = labelY + 24;
        for (int i = 0; i < FONT_LABELS.length; i++) {
            if (isInside(mouseX, mouseY, x + 12, fontY, PANEL_W - 24, 34)) {
                selected = i;
                ClickGuiMod mod = ClickGuiMod.get();
                if (mod != null) {
                    mod.setFontFace(i);
                }
                return true;
            }
            fontY += 34 + 4;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void drawButton(DrawContext ctx, int x, int y, int w, int h, String label, int mouseX, int mouseY) {
        boolean hovered = isInside(mouseX, mouseY, x, y, w, h);
        ctx.fill(x, y, x + w, y + h, hovered ? 0xFF1B2634 : 0xFF101821);
        drawOutline(ctx, x, y, w, h, 0xFF263241);
        int labelW = CustomFontRenderer.getStringWidth(textRenderer, label);
        CustomFontRenderer.drawString(ctx, textRenderer, label, x + w / 2 - labelW / 2, y + 6, 0xFFFFFFFF);
    }

    private void drawOutline(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private boolean isInside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
