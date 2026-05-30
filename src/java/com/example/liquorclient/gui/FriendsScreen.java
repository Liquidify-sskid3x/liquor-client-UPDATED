package com.example.liquorclient.gui;

import com.example.liquorclient.render.ClickGuiMod;
import com.example.liquorclient.utility.CustomFontRenderer;
import com.example.liquorclient.utility.FriendManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class FriendsScreen extends Screen {
    private static final int PANEL_W = 380;
    private static final int PANEL_H = 280;

    private final Screen parent;
    private String friendName = "";
    private boolean inputFocused = false;
    private double scroll = 0.0;

    public FriendsScreen(Screen parent) {
        super(Text.literal("Friends"));
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
        CustomFontRenderer.drawString(ctx, textRenderer, "Friends", x + 12, y + 12, 0xFFFFFFFF);

        int inputX = x + 12;
        int inputY = y + 36;
        int inputW = PANEL_W - 96;
        ctx.fill(inputX, inputY, inputX + inputW, inputY + 20, inputFocused ? 0xFF152233 : 0xFF101821);
        drawOutline(ctx, inputX, inputY, inputW, 20, inputFocused ? accent : 0xFF263241);
        String label = friendName.isEmpty() && !inputFocused ? "friend name" : friendName;
        CustomFontRenderer.drawString(ctx, textRenderer, label, inputX + 6, inputY + 6, friendName.isEmpty() ? 0xFF6E7A8A : 0xFFFFFFFF);

        drawButton(ctx, x + PANEL_W - 74, inputY, 60, 20, "Add", mouseX, mouseY);

        int listX = x + 12;
        int listY = y + 66;
        int listW = PANEL_W - 24;
        int listH = PANEL_H - 106;

        ctx.fill(listX, listY, listX + listW, listY + listH, 0xFF080B10);

        List<String> friends = new ArrayList<>(FriendManager.getFriends());
        int totalH = friends.size() * 24;
        int maxScroll = Math.max(0, totalH - listH + 4);
        if (scroll > maxScroll) scroll = maxScroll;
        if (scroll < 0) scroll = 0;

        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(0, 0);

        int cy = listY + 2 - (int) scroll;
        for (String name : friends) {
            if (cy + 22 < listY || cy > listY + listH) {
                cy += 24;
                continue;
            }
            ctx.fill(listX + 2, cy, listX + listW - 2, cy + 22, 0xFF0F151D);
            CustomFontRenderer.drawString(ctx, textRenderer, name, listX + 10, cy + 7, 0xFFC4CAD3);

            int delX = listX + listW - 56;
            drawButton(ctx, delX, cy + 2, 44, 18, "Delete", mouseX, mouseY);

            cy += 24;
        }

        ctx.getMatrices().popMatrix();

        if (maxScroll > 0) {
            int barH = (int) (listH * Math.min(1.0, (double) listH / totalH));
            int barY = listY + (int) ((scroll / maxScroll) * (listH - barH));
            ctx.fill(listX + listW - 3, listY, listX + listW, listY + listH, 0xFF1B2430);
            ctx.fill(listX + listW - 3, barY, listX + listW, barY + barH, accent);
        }

        drawButton(ctx, x + 12, y + PANEL_H - 30, 60, 20, "Back", mouseX, mouseY);
        if (!friends.isEmpty()) {
            drawButton(ctx, x + 78, y + PANEL_H - 30, 60, 20, "Clear", mouseX, mouseY);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return super.mouseClicked(click, doubled);

        int x = width / 2 - PANEL_W / 2;
        int y = height / 2 - PANEL_H / 2;

        int inputX = x + 12;
        int inputY = y + 36;
        int inputW = PANEL_W - 96;
        inputFocused = isInside(mx, my, inputX, inputY, inputW, 20);

        if (isInside(mx, my, x + PANEL_W - 74, inputY, 60, 20)) {
            addFriend();
            return true;
        }

        if (isInside(mx, my, x + 12, y + PANEL_H - 30, 60, 20)) {
            close();
            return true;
        }

        if (isInside(mx, my, x + 78, y + PANEL_H - 30, 60, 20)) {
            FriendManager.clear();
            return true;
        }

        int listX = x + 12;
        int listY = y + 66;
        int listW = PANEL_W - 24;
        int cy = listY + 2 - (int) scroll;
        for (String name : FriendManager.getFriends()) {
            int delX = listX + listW - 56;
            if (isInside(mx, my, delX, cy + 2, 44, 18)) {
                FriendManager.removeFriend(name);
                return true;
            }
            cy += 24;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        List<String> friends = new ArrayList<>(FriendManager.getFriends());
        int listY = height / 2 - PANEL_H / 2 + 66;
        int listH = PANEL_H - 106;
        int totalH = friends.size() * 24;
        int maxScroll = Math.max(0, totalH - listH + 4);
        scroll -= verticalAmount * 16;
        scroll = Math.max(0, Math.min(maxScroll, scroll));
        return true;
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!inputFocused) return super.charTyped(input);
        if (friendName.length() >= 32) return true;
        String text = input.asString();
        if (text.length() == 1) {
            char chr = text.charAt(0);
            if (Character.isLetterOrDigit(chr) || chr == '_' || chr == '-' || chr == ' ') {
                friendName += chr;
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        int key = input.key();
        if (inputFocused) {
            if (key == GLFW.GLFW_KEY_BACKSPACE && !friendName.isEmpty()) {
                friendName = friendName.substring(0, friendName.length() - 1);
                return true;
            }
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                addFriend();
                return true;
            }
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                friendName = "";
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

    private void addFriend() {
        String name = friendName.trim();
        if (!name.isEmpty()) {
            FriendManager.addFriend(name);
            friendName = "";
            inputFocused = false;
        }
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
