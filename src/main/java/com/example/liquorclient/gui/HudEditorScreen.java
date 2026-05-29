package com.example.liquorclient.gui;

import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.ModuleManager;
import com.example.liquorclient.render.ClickGuiMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class HudEditorScreen extends Screen {
    private final Screen parent;
    private DragTarget dragTarget = DragTarget.NONE;
    private int dragOffsetX;
    private int dragOffsetY;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int accent = ClickGuiMod.accentColor(System.currentTimeMillis());

        ctx.fill(0, 0, width, height, 0xAA000000);

        int grid = 8;
        for (int xG = 0; xG < width; xG += grid) {
            ctx.fill(xG, 0, xG + 1, height, 0x07FFFFFF);
        }
        for (int yG = 0; yG < height; yG += grid) {
            ctx.fill(0, yG, width, yG + 1, 0x07FFFFFF);
        }

        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("HUD Editor"), width / 2, 14, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Drag elements. Clamping prevents off-screen."), width / 2, 28, 0xFF9BA8B7);

        drawPreview(ctx, "Info", ArrayListHud.getInfoX(), ArrayListHud.getInfoY(), ArrayListHud.getInfoWidth(), ArrayListHud.getInfoHeight(), accent);
        drawPreview(ctx, "ArrayList", getArrayX(), ArrayListHud.getArrayY(), ArrayListHud.getMaxArrayWidth(), ArrayListHud.getArrayHeight(), accent);
        drawPreview(ctx, "Armor", ArrayListHud.getArmorX(), ArrayListHud.getArmorY(), ArrayListHud.getArmorWidth(), ArrayListHud.getArmorHeight(), accent);
        drawPreview(ctx, "Potions", ArrayListHud.getPotionX(), ArrayListHud.getPotionY(), ArrayListHud.getPotionWidth(), ArrayListHud.getPotionHeight(), accent);
        drawPreview(ctx, "Keystrokes", ArrayListHud.getKeystrokesX(), ArrayListHud.getKeystrokesY(), ArrayListHud.getKeystrokesWidth(), ArrayListHud.getKeystrokesHeight(), accent);
        drawPreview(ctx, "CPS", ArrayListHud.getCpsX(), ArrayListHud.getCpsY(), ArrayListHud.getCpsWidth(), ArrayListHud.getCpsHeight(), accent);

        drawButton(ctx, 8, height - 28, 72, 20, "Back", mouseX, mouseY);
        drawButton(ctx, 86, height - 28, 72, 20, "Reset", mouseX, mouseY);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return super.mouseClicked(click, doubled);

        if (isInside(mouseX, mouseY, 8, height - 28, 72, 20)) { close(); return true; }
        if (isInside(mouseX, mouseY, 86, height - 28, 72, 20)) { ArrayListHud.resetPositions(); return true; }

        if (tryDrag(mouseX, mouseY, ArrayListHud.getInfoX(), ArrayListHud.getInfoY(), ArrayListHud.getInfoWidth(), ArrayListHud.getInfoHeight(), DragTarget.INFO)) return true;
        if (tryDrag(mouseX, mouseY, getArrayX(), ArrayListHud.getArrayY(), ArrayListHud.getMaxArrayWidth(), ArrayListHud.getArrayHeight(), DragTarget.ARRAY)) return true;
        if (tryDrag(mouseX, mouseY, ArrayListHud.getArmorX(), ArrayListHud.getArmorY(), ArrayListHud.getArmorWidth(), ArrayListHud.getArmorHeight(), DragTarget.ARMOR)) return true;
        if (tryDrag(mouseX, mouseY, ArrayListHud.getPotionX(), ArrayListHud.getPotionY(), ArrayListHud.getPotionWidth(), ArrayListHud.getPotionHeight(), DragTarget.POTION)) return true;
        if (tryDrag(mouseX, mouseY, ArrayListHud.getKeystrokesX(), ArrayListHud.getKeystrokesY(), ArrayListHud.getKeystrokesWidth(), ArrayListHud.getKeystrokesHeight(), DragTarget.KEYSTROKES)) return true;
        if (tryDrag(mouseX, mouseY, ArrayListHud.getCpsX(), ArrayListHud.getCpsY(), ArrayListHud.getCpsWidth(), ArrayListHud.getCpsHeight(), DragTarget.CPS)) return true;

        return super.mouseClicked(click, doubled);
    }

    private boolean tryDrag(double mx, double my, int x, int y, int w, int h, DragTarget target) {
        if (isInside(mx, my, x, y, w, h)) {
            dragTarget = target;
            dragOffsetX = (int) mx - x;
            dragOffsetY = (int) my - y;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT || dragTarget == DragTarget.NONE) return super.mouseDragged(click, deltaX, deltaY);

        int targetX = (int) click.x() - dragOffsetX;
        int targetY = (int) click.y() - dragOffsetY;
        targetX = Math.round((float) targetX / 4.0f) * 4;
        targetY = Math.round((float) targetY / 4.0f) * 4;

        int p = 0; // Use 0 padding for perfect corner alignment
        switch (dragTarget) {
            case INFO -> ArrayListHud.setInfoPosition(clamp(targetX, p, width - ArrayListHud.getInfoWidth()), clamp(targetY, p, height - ArrayListHud.getInfoHeight()));
            case ARRAY -> ArrayListHud.setArrayPosition(clamp(targetX, p, width - ArrayListHud.getMaxArrayWidth()), clamp(targetY, p, height - ArrayListHud.getArrayHeight()));
            case ARMOR -> ArrayListHud.setArmorPosition(clamp(targetX, p, width - ArrayListHud.getArmorWidth()), clamp(targetY, p, height - ArrayListHud.getArmorHeight()));
            case POTION -> ArrayListHud.setPotionPosition(clamp(targetX, p, width - ArrayListHud.getPotionWidth()), clamp(targetY, p, height - ArrayListHud.getPotionHeight()));
            case KEYSTROKES -> ArrayListHud.setKeystrokesPosition(clamp(targetX, p, width - ArrayListHud.getKeystrokesWidth()), clamp(targetY, p, height - ArrayListHud.getKeystrokesHeight()));
            case CPS -> ArrayListHud.setCpsPosition(clamp(targetX, p, width - ArrayListHud.getCpsWidth()), clamp(targetY, p, height - ArrayListHud.getCpsHeight()));
        }
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) dragTarget = DragTarget.NONE;
        return super.mouseReleased(click);
    }

    private void drawPreview(DrawContext ctx, String name, int x, int y, int w, int h, int accent) {
        ctx.fill(x, y, x + w, y + h, 0xAA101821);
        ctx.fill(x, y, x + 1, y + h, accent);
        ctx.fill(x + w - 1, y, x + w, y + h, 0x44FFFFFF);
        ctx.fill(x, y, x + w, y + 1, 0x44FFFFFF);
        ctx.fill(x, y + h - 1, x + w, y + h, 0x44FFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.literal(name), x + 4, y + 4, 0xFFFFFFFF);
    }

    private void drawButton(DrawContext ctx, int x, int y, int w, int h, String label, int mouseX, int mouseY) {
        boolean hovered = isInside(mouseX, mouseY, x, y, w, h);
        ctx.fill(x, y, x + w, y + h, hovered ? 0xFF1B2634 : 0xFF101821);
        int labelW = textRenderer.getWidth(label);
        ctx.drawTextWithShadow(textRenderer, Text.literal(label), x + w / 2 - labelW / 2, y + 6, 0xFFFFFFFF);
    }

    private int getArrayX() {
        int ax = ArrayListHud.getArrayX();
        return ax < 0 ? width - ArrayListHud.getMaxArrayWidth() - 8 : ax;
    }

    private boolean isInside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override public boolean shouldPause() { return false; }

    private enum DragTarget {
        NONE, INFO, ARRAY, ARMOR, POTION, KEYSTROKES, CPS
    }
}
