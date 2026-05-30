package com.example.liquorclient.gui;

import com.example.liquorclient.accounts.CrackedAccount;
import com.example.liquorclient.accounts.CrackedAccountManager;
import com.example.liquorclient.render.ClickGuiMod;
import com.example.liquorclient.utility.CustomFontRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class CrackedAccountsScreen extends Screen {
    private static final int PANEL_W = 390;
    private static final int PANEL_H = 268;

    private final Screen parent;
    private String newAccountName = "";
    private boolean inputFocused = false;

    public CrackedAccountsScreen(Screen parent) {
        super(Text.literal("Cracked Accounts"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int accent = ClickGuiMod.accentColor(System.currentTimeMillis());
        int x = width / 2 - PANEL_W / 2;
        int y = height / 2 - PANEL_H / 2;

        ctx.fill(0, 0, width, height, 0x88000000);
        ctx.fill(x, y, x + PANEL_W, y + PANEL_H, 0xFF0B1017);
        ctx.fill(x, y, x + PANEL_W, y + 2, accent);
        CustomFontRenderer.drawString(ctx, textRenderer, "Cracked Accounts", x + 12, y + 12, 0xFFFFFFFF);
        CustomFontRenderer.drawString(ctx, textRenderer, "Selected: " + CrackedAccountManager.getSelectedName(), x + 12, y + 27, 0xFF9BA8B7);

        int inputX = x + 12;
        int inputY = y + 48;
        int inputW = PANEL_W - 104;
        ctx.fill(inputX, inputY, inputX + inputW, inputY + 20, inputFocused ? 0xFF152233 : 0xFF101821);
        drawOutline(ctx, inputX, inputY, inputW, 20, inputFocused ? accent : 0xFF263241);
        String label = newAccountName.isEmpty() && !inputFocused ? "DevPlayer" : newAccountName;
        CustomFontRenderer.drawString(ctx, textRenderer, label, inputX + 6, inputY + 6, newAccountName.isEmpty() ? 0xFF6E7A8A : 0xFFFFFFFF);
        drawButton(ctx, x + PANEL_W - 82, inputY, 70, 20, "Add", mouseX, mouseY);

        int rowY = y + 80;
        List<CrackedAccount> accounts = CrackedAccountManager.getAccounts();
        for (CrackedAccount account : accounts) {
            boolean selected = account.name().equalsIgnoreCase(CrackedAccountManager.getSelectedName());
            ctx.fill(x + 12, rowY, x + PANEL_W - 12, rowY + 28, selected ? 0xFF132014 : 0xFF0F151D);
            ctx.fill(x + 12, rowY, x + 14, rowY + 28, selected ? accent : 0xFF263241);
            CustomFontRenderer.drawString(ctx, textRenderer, account.name(), x + 22, rowY + 5, selected ? 0xFFE9FFE6 : 0xFFC4CAD3);
            CustomFontRenderer.drawString(ctx, textRenderer, shortUuid(account.uuid().toString()), x + 22, rowY + 17, 0xFF7D8998);

            drawButton(ctx, x + PANEL_W - 184, rowY + 5, 48, 18, "Use", mouseX, mouseY);
            drawButton(ctx, x + PANEL_W - 130, rowY + 5, 50, 18, "Args", mouseX, mouseY);
            drawButton(ctx, x + PANEL_W - 74, rowY + 5, 52, 18, "Delete", mouseX, mouseY);
            rowY += 32;
            if (rowY > y + PANEL_H - 40) break;
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
        int inputX = x + 12;
        int inputY = y + 48;
        int inputW = PANEL_W - 104;
        inputFocused = isInside(mouseX, mouseY, inputX, inputY, inputW, 20);

        if (isInside(mouseX, mouseY, x + PANEL_W - 82, inputY, 70, 20)) {
            addAccount();
            return true;
        }
        if (isInside(mouseX, mouseY, x + 12, y + PANEL_H - 30, 70, 20)) {
            close();
            return true;
        }

        int rowY = y + 80;
        for (CrackedAccount account : CrackedAccountManager.getAccounts()) {
            if (isInside(mouseX, mouseY, x + PANEL_W - 184, rowY + 5, 48, 18)) {
                CrackedAccountManager.select(account.name());
                return true;
            }
            if (isInside(mouseX, mouseY, x + PANEL_W - 130, rowY + 5, 50, 18)) {
                copyLaunchArgs(account);
                return true;
            }
            if (isInside(mouseX, mouseY, x + PANEL_W - 74, rowY + 5, 52, 18)) {
                CrackedAccountManager.delete(account.name());
                return true;
            }
            rowY += 32;
            if (rowY > y + PANEL_H - 40) break;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!inputFocused) return super.charTyped(input);
        if (newAccountName.length() >= 16) return true;
        String text = input.asString();
        if (text.length() == 1) {
            char chr = text.charAt(0);
            if (Character.isLetterOrDigit(chr) || chr == '_') {
                newAccountName += chr;
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        int key = input.key();
        if (inputFocused) {
            if (key == GLFW.GLFW_KEY_BACKSPACE && !newAccountName.isEmpty()) {
                newAccountName = newAccountName.substring(0, newAccountName.length() - 1);
                return true;
            }
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                addAccount();
                return true;
            }
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                inputFocused = false;
                return true;
            }
        }

        if (key == GLFW.GLFW_KEY_ESCAPE) {
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

    private void addAccount() {
        if (CrackedAccountManager.add(newAccountName)) {
            newAccountName = "";
            inputFocused = false;
        }
    }

    private void copyLaunchArgs(CrackedAccount account) {
        String args = "--username " + account.name() + " --uuid " + account.uuid();
        GLFW.glfwSetClipboardString(MinecraftClient.getInstance().getWindow().getHandle(), args);
        NotificationManager.push("Cracked Account", "Copied run args");
    }

    private String shortUuid(String uuid) {
        return uuid.length() <= 18 ? uuid : uuid.substring(0, 18) + "...";
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
