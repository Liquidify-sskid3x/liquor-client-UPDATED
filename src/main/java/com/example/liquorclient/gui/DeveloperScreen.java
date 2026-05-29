package com.example.liquorclient.gui;

import com.example.liquorclient.config.ConfigManager;
import com.example.liquorclient.module.ModuleManager;
import com.example.liquorclient.render.ClickGuiMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DeveloperScreen extends Screen {
    private static final int PANEL_W = 520;
    private static final int PANEL_H = 330;

    private final Screen parent;
    private final Path latestLog = FabricLoader.getInstance().getGameDir().resolve("logs").resolve("latest.log");
    private List<String> lines = new ArrayList<>();
    private double scroll = 0.0;

    public DeveloperScreen(Screen parent) {
        super(Text.literal("Developer"));
        this.parent = parent;
        reload();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int accent = ClickGuiMod.accentColor(System.currentTimeMillis());
        int x = width / 2 - PANEL_W / 2;
        int y = height / 2 - PANEL_H / 2;

        ctx.fill(0, 0, width, height, 0x88000000);
        ctx.fill(x, y, x + PANEL_W, y + PANEL_H, 0xFF0B1017);
        ctx.fill(x, y, x + PANEL_W, y + 2, accent);

        ctx.drawTextWithShadow(textRenderer, Text.literal("Developer"), x + 12, y + 12, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Game: " + FabricLoader.getInstance().getGameDir()), x + 12, y + 28, 0xFF9BA8B7);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Config: " + ConfigManager.getConfigDir()), x + 12, y + 40, 0xFF9BA8B7);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Modules: " + ModuleManager.getModules().size()), x + 12, y + 52, 0xFF9BA8B7);

        drawButton(ctx, x + PANEL_W - 214, y + 10, 62, 20, "Reload", mouseX, mouseY);
        drawButton(ctx, x + PANEL_W - 146, y + 10, 62, 20, "Copy", mouseX, mouseY);
        drawButton(ctx, x + PANEL_W - 78, y + 10, 62, 20, "Back", mouseX, mouseY);

        int logX = x + 12;
        int logY = y + 74;
        int logW = PANEL_W - 24;
        int logH = PANEL_H - 88;
        ctx.fill(logX, logY, logX + logW, logY + logH, 0xFF070A0F);
        drawOutline(ctx, logX, logY, logW, logH, 0xFF263241);

        int maxScroll = Math.max(0, lines.size() * 11 - (logH - 10));
        if (scroll > maxScroll) scroll = maxScroll;
        ctx.enableScissor(logX + 1, logY + 1, logX + logW - 1, logY + logH - 1);
        int lineY = logY + 6 - (int) scroll;
        for (String line : lines) {
            if (lineY > logY - 12 && lineY < logY + logH) {
                ctx.drawTextWithShadow(textRenderer, Text.literal(trimToWidth(line, logW - 12)), logX + 6, lineY, 0xFFD8DEE8);
            }
            lineY += 11;
        }
        ctx.disableScissor();

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return super.mouseClicked(click, doubled);
        double mouseX = click.x();
        double mouseY = click.y();
        int x = width / 2 - PANEL_W / 2;
        int y = height / 2 - PANEL_H / 2;

        if (isInside(mouseX, mouseY, x + PANEL_W - 214, y + 10, 62, 20)) {
            reload();
            return true;
        }
        if (isInside(mouseX, mouseY, x + PANEL_W - 146, y + 10, 62, 20)) {
            GLFW.glfwSetClipboardString(MinecraftClient.getInstance().getWindow().getHandle(), latestLog.toString());
            NotificationManager.push("Developer", "Copied log path");
            return true;
        }
        if (isInside(mouseX, mouseY, x + PANEL_W - 78, y + 10, 62, 20)) {
            close();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll = Math.max(0.0, scroll - verticalAmount * 18.0);
        return true;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (input.key() == GLFW.GLFW_KEY_F5) {
            reload();
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

    private void reload() {
        try {
            List<String> all = Files.exists(latestLog)
                    ? Files.readAllLines(latestLog, StandardCharsets.UTF_8)
                    : List.of("latest.log does not exist yet: " + latestLog);
            int from = Math.max(0, all.size() - 220);
            lines = new ArrayList<>(all.subList(from, all.size()));
            scroll = Math.max(0, lines.size() * 11 - (PANEL_H - 98));
        } catch (IOException ex) {
            lines = List.of("Could not read latest.log: " + ex.getMessage());
        }
    }

    private String trimToWidth(String value, int maxWidth) {
        if (textRenderer.getWidth(value) <= maxWidth) return value;
        String trimmed = value;
        while (trimmed.length() > 4 && textRenderer.getWidth(trimmed + "...") > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + "...";
    }

    private void drawButton(DrawContext ctx, int x, int y, int w, int h, String label, int mouseX, int mouseY) {
        boolean hovered = isInside(mouseX, mouseY, x, y, w, h);
        ctx.fill(x, y, x + w, y + h, hovered ? 0xFF1B2634 : 0xFF101821);
        drawOutline(ctx, x, y, w, h, 0xFF263241);
        int labelW = textRenderer.getWidth(label);
        ctx.drawTextWithShadow(textRenderer, Text.literal(label), x + w / 2 - labelW / 2, y + 6, 0xFFFFFFFF);
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
