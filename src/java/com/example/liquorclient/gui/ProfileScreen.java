package com.example.liquorclient.gui;

import com.example.liquorclient.config.ConfigManager;
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

public class ProfileScreen extends Screen {
    private static final int PANEL_W = 400;
    private static final int PANEL_H = 280;

    private final Screen parent;
    private String newProfileName = "";
    private String renameTarget = null;
    private boolean inputFocused = false;
    private boolean importMode = false;

    public ProfileScreen(Screen parent) {
        super(Text.literal("Profiles"));
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
        CustomFontRenderer.drawString(ctx, textRenderer, "Profiles", x + 12, y + 12, 0xFFFFFFFF);

        if (importMode) {
            drawImportMode(ctx, x, y, mouseX, mouseY, accent);
        } else {
            drawNormalMode(ctx, x, y, mouseX, mouseY, accent);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawNormalMode(DrawContext ctx, int x, int y, int mouseX, int mouseY, int accent) {
        CustomFontRenderer.drawString(ctx, textRenderer, "Current: " + ConfigManager.getCurrentProfile(), x + 12, y + 27, 0xFF9BA8B7);

        int inputX = x + 12;
        int inputY = y + 48;
        int inputW = PANEL_W - 104;
        ctx.fill(inputX, inputY, inputX + inputW, inputY + 20, inputFocused ? 0xFF152233 : 0xFF101821);
        drawOutline(ctx, inputX, inputY, inputW, 20, inputFocused ? accent : 0xFF263241);

        if (renameTarget != null) {
            String label = newProfileName.isEmpty() && !inputFocused ? "new name for " + renameTarget : newProfileName;
            CustomFontRenderer.drawString(ctx, textRenderer, label, inputX + 6, inputY + 6, newProfileName.isEmpty() ? 0xFF6E7A8A : 0xFFFFFFFF);
            drawButton(ctx, x + PANEL_W - 82, inputY, 70, 20, "Rename", mouseX, mouseY);
            CustomFontRenderer.drawString(ctx, textRenderer, "Renaming: " + renameTarget, x + 12, y + 74, 0xFFBB8844);
        } else {
            String label = newProfileName.isEmpty() && !inputFocused ? "new_profile" : newProfileName;
            CustomFontRenderer.drawString(ctx, textRenderer, label, inputX + 6, inputY + 6, newProfileName.isEmpty() ? 0xFF6E7A8A : 0xFFFFFFFF);
            drawButton(ctx, x + PANEL_W - 82, inputY, 70, 20, "Add", mouseX, mouseY);
        }

        int listY = renameTarget != null ? y + 78 : y + 74;
        int rowY = listY;
        List<String> profiles = ConfigManager.getProfiles();
        for (String profile : profiles) {
            boolean current = profile.equals(ConfigManager.getCurrentProfile());
            ctx.fill(x + 12, rowY, x + PANEL_W - 12, rowY + 24, current ? 0xFF132014 : 0xFF0F151D);
            ctx.fill(x + 12, rowY, x + 14, rowY + 24, current ? accent : 0xFF263241);
            CustomFontRenderer.drawString(ctx, textRenderer, profile, x + 22, rowY + 8, current ? 0xFFE9FFE6 : 0xFFC4CAD3);

            drawButton(ctx, x + PANEL_W - 182, rowY + 3, 44, 18, "Use", mouseX, mouseY);
            drawButton(ctx, x + PANEL_W - 132, rowY + 3, 50, 18, "Rename", mouseX, mouseY);
            drawButton(ctx, x + PANEL_W - 74, rowY + 3, 52, 18, "Delete", mouseX, mouseY);
            rowY += 28;
            if (rowY > y + PANEL_H - 40) break;
        }

        drawButton(ctx, x + 12, y + PANEL_H - 30, 70, 20, "Back", mouseX, mouseY);
        drawButton(ctx, x + 88, y + PANEL_H - 30, 70, 20, "Export", mouseX, mouseY);
        drawButton(ctx, x + 164, y + PANEL_H - 30, 70, 20, "Import", mouseX, mouseY);
    }

    private void drawImportMode(DrawContext ctx, int x, int y, int mouseX, int mouseY, int accent) {
        CustomFontRenderer.drawString(ctx, textRenderer, "Select a file to import", x + 12, y + 27, 0xFF9BA8B7);

        int rowY = y + 48;
        List<String> exported = ConfigManager.getExportedProfiles();
        if (exported.isEmpty()) {
            CustomFontRenderer.drawString(ctx, textRenderer, "No exported profiles found", x + 22, rowY, 0xFF6E7A8A);
        } else {
            for (String file : exported) {
                ctx.fill(x + 12, rowY, x + PANEL_W - 12, rowY + 24, 0xFF0F151D);
                ctx.fill(x + 12, rowY, x + 14, rowY + 24, 0xFF263241);
                CustomFontRenderer.drawString(ctx, textRenderer, file, x + 22, rowY + 8, 0xFFC4CAD3);
                drawButton(ctx, x + PANEL_W - 74, rowY + 3, 52, 18, "Import", mouseX, mouseY);
                rowY += 28;
                if (rowY > y + PANEL_H - 40) break;
            }
        }

        drawButton(ctx, x + 12, y + PANEL_H - 30, 80, 20, "Back", mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return super.mouseClicked(click, doubled);

        int x = width / 2 - PANEL_W / 2;
        int y = height / 2 - PANEL_H / 2;

        if (importMode) {
            if (handleImportClick(mouseX, mouseY, x, y)) return true;
        } else {
            if (handleNormalClick(mouseX, mouseY, x, y)) return true;
        }

        return super.mouseClicked(click, doubled);
    }

    private boolean handleNormalClick(double mouseX, double mouseY, int x, int y) {
        int inputX = x + 12;
        int inputY = y + 48;
        int inputW = PANEL_W - 104;
        inputFocused = isInside(mouseX, mouseY, inputX, inputY, inputW, 20);

        if (isInside(mouseX, mouseY, x + PANEL_W - 82, inputY, 70, 20)) {
            if (renameTarget != null) {
                doRename();
            } else {
                addProfile();
            }
            return true;
        }
        if (isInside(mouseX, mouseY, x + 12, y + PANEL_H - 30, 70, 20)) {
            close();
            return true;
        }
        if (isInside(mouseX, mouseY, x + 88, y + PANEL_H - 30, 70, 20)) {
            exportProfile();
            return true;
        }
        if (isInside(mouseX, mouseY, x + 164, y + PANEL_H - 30, 70, 20)) {
            importMode = true;
            renameTarget = null;
            newProfileName = "";
            return true;
        }

        int listY = renameTarget != null ? y + 78 : y + 74;
        int rowY = listY;
        for (String profile : ConfigManager.getProfiles()) {
            if (isInside(mouseX, mouseY, x + PANEL_W - 182, rowY + 3, 44, 18)) {
                ConfigManager.switchProfile(profile);
                return true;
            }
            if (isInside(mouseX, mouseY, x + PANEL_W - 132, rowY + 3, 50, 18)) {
                startRename(profile);
                return true;
            }
            if (isInside(mouseX, mouseY, x + PANEL_W - 74, rowY + 3, 52, 18)) {
                ConfigManager.deleteProfile(profile);
                return true;
            }
            rowY += 28;
            if (rowY > y + PANEL_H - 40) break;
        }

        return false;
    }

    private boolean handleImportClick(double mouseX, double mouseY, int x, int y) {
        if (isInside(mouseX, mouseY, x + 12, y + PANEL_H - 30, 80, 20)) {
            importMode = false;
            return true;
        }

        int rowY = y + 48;
        List<String> exported = ConfigManager.getExportedProfiles();
        for (String file : exported) {
            if (isInside(mouseX, mouseY, x + PANEL_W - 74, rowY + 3, 52, 18)) {
                ConfigManager.importProfile(file);
                importMode = false;
                return true;
            }
            rowY += 28;
            if (rowY > y + PANEL_H - 40) break;
        }

        return false;
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!inputFocused) return super.charTyped(input);
        if (newProfileName.length() >= 32) return true;
        String text = input.asString();
        if (text.length() == 1) {
            char chr = text.charAt(0);
            if (Character.isLetterOrDigit(chr) || chr == '_' || chr == '-') {
                newProfileName += chr;
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        int key = input.key();
        if (inputFocused) {
            if (key == GLFW.GLFW_KEY_BACKSPACE && !newProfileName.isEmpty()) {
                newProfileName = newProfileName.substring(0, newProfileName.length() - 1);
                return true;
            }
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                if (renameTarget != null) {
                    doRename();
                } else {
                    addProfile();
                }
                return true;
            }
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                renameTarget = null;
                newProfileName = "";
                inputFocused = false;
                return true;
            }
        }

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (importMode) {
                importMode = false;
                return true;
            }
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

    private void addProfile() {
        if (ConfigManager.createProfile(newProfileName)) {
            newProfileName = "";
            inputFocused = false;
        }
    }

    private void exportProfile() {
        ConfigManager.exportProfile(ConfigManager.getCurrentProfile());
    }

    private void startRename(String profile) {
        renameTarget = profile;
        newProfileName = "";
        inputFocused = true;
    }

    private void doRename() {
        if (renameTarget != null && ConfigManager.renameProfile(renameTarget, newProfileName)) {
            renameTarget = null;
            newProfileName = "";
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
