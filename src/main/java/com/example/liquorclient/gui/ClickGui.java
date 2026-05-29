package com.example.liquorclient.gui;

import com.example.liquorclient.config.ConfigManager;
import com.example.liquorclient.module.BooleanSetting;
import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.ColorSetting;
import com.example.liquorclient.module.KeybindSetting;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.ModuleManager;
import com.example.liquorclient.module.NumberSetting;
import com.example.liquorclient.module.Setting;
import com.example.liquorclient.render.ClickGuiMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClickGui extends Screen {
    private static final int PANEL_W = 460;
    private static final int PANEL_H = 300;
    private static final int SIDEBAR_W = 112;

    private static Category selectedCategory = Category.COMBAT;
    private Module settingsModule = null;
    private Setting<?> listeningSetting = null;
    private String searchText = "";
    private boolean searchFocused = false;

    private boolean lastLeftDown = false;
    private boolean lastRightDown = false;
    private double settingsScroll = 0.0;
    private double modulesScroll = 0.0;

    public ClickGui() {
        super(Text.literal("Liquor Client"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        long window = mc.getWindow().getHandle();

        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rightDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        int accent = ClickGuiMod.accentColor(System.currentTimeMillis());

        int x = width / 2 - PANEL_W / 2;
        int y = height / 2 - PANEL_H / 2;

        ctx.fill(0, 0, width, height, 0x76000000);
        ctx.fill(x + 5, y + 6, x + PANEL_W + 5, y + PANEL_H + 6, 0x66000000);
        ctx.fill(x, y, x + PANEL_W, y + PANEL_H, 0xFF0B1017);
        // Accent bar at the very top
        ctx.fill(x, y, x + PANEL_W, y + 2, accent);
        
        // Sidebar background
        ctx.fill(x, y + 2, x + SIDEBAR_W, y + PANEL_H, 0xFF080B10);
        // Sidebar separator
        ctx.fill(x + SIDEBAR_W, y + 2, x + SIDEBAR_W + 1, y + PANEL_H, 0xFF1B2430);

        // Logo with tighter positioning to avoid "seepage"
        drawScaledText(ctx, "LIQUOR", x + 14, y + 14, 1.4F, accent);
        ctx.drawTextWithShadow(textRenderer, Text.literal("client"), x + 15, y + 34, 0xFF667386);

        drawCategories(ctx, x, y, mouseX, mouseY, leftDown, accent);
        drawUtilityButtons(ctx, x, y, mouseX, mouseY, leftDown);

        if (settingsModule == null) {
            drawModules(ctx, x, y, mouseX, mouseY, leftDown, rightDown, accent);
        } else {
            drawSettings(ctx, x, y, mouseX, mouseY, leftDown, accent);
        }

        lastLeftDown = leftDown;
        lastRightDown = rightDown;

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawCategories(DrawContext ctx, int x, int y, double mx, double my, boolean leftDown, int accent) {
        int rowX = x + 8;
        int rowW = SIDEBAR_W - 16;
        int cy = y + 58;

        for (Category cat : Category.values()) {
            if (ModuleManager.getByCategory(cat).isEmpty())
                continue;

            boolean hovered = isInside(mx, my, rowX, cy, rowW, 22);
            boolean selected = cat == selectedCategory;

            if (selected) {
                ctx.fill(rowX, cy, rowX + rowW, cy + 22, 0xFF132014);
                ctx.fill(rowX, cy, rowX + 2, cy + 22, accent);
            } else if (hovered) {
                ctx.fill(rowX, cy, rowX + rowW, cy + 22, 0xFF111821);
            }

            String name = titleCase(cat.name());
            ctx.drawTextWithShadow(textRenderer, Text.literal(name), rowX + 8, cy + 7,
                    selected ? 0xFFE7FFE3 : 0xFF8792A0);

            if (hovered && leftDown && !lastLeftDown) {
                selectedCategory = cat;
                settingsModule = null;
                listeningSetting = null;
                searchFocused = false;
                settingsScroll = 0.0;
                modulesScroll = 0.0;
            }

            cy += 25;
        }
    }

    private void drawUtilityButtons(DrawContext ctx, int x, int y, double mx, double my, boolean leftDown) {
        int rowX = x + 8;
        int rowW = SIDEBAR_W - 16;
        int profileY = y + PANEL_H - 58;
        int hudY = y + PANEL_H - 32;

        drawSidebarButton(ctx, rowX, profileY, rowW, 20, "Profiles", mx, my);
        drawSidebarButton(ctx, rowX, hudY, rowW, 20, "HUD Editor", mx, my);
        ctx.drawTextWithShadow(textRenderer, Text.literal(ConfigManager.getCurrentProfile()), rowX + 4, profileY - 11,
                0xFF667386);

        if (leftDown && !lastLeftDown) {
            if (isInside(mx, my, rowX, profileY, rowW, 20)) {
                MinecraftClient.getInstance().setScreen(new ProfileScreen(this));
            } else if (isInside(mx, my, rowX, hudY, rowW, 20)) {
                MinecraftClient.getInstance().setScreen(new HudEditorScreen(this));
            }
        }
    }

    private void drawSidebarButton(DrawContext ctx, int x, int y, int w, int h, String label, double mx, double my) {
        boolean hovered = isInside(mx, my, x, y, w, h);
        ctx.fill(x, y, x + w, y + h, hovered ? 0xFF162131 : 0xFF101821);
        drawOutline(ctx, x, y, w, h, 0xFF1C2633);
        int labelW = textRenderer.getWidth(label);
        ctx.drawTextWithShadow(textRenderer, Text.literal(label), x + w / 2 - labelW / 2, y + 6, 0xFFC4CAD3);
    }

    private void drawModules(DrawContext ctx, int x, int y, double mx, double my, boolean leftDown, boolean rightDown,
            int accent) {
        int startX = x + SIDEBAR_W + 14;
        int contentW = PANEL_W - SIDEBAR_W - 28;
        int cy = y + 68;

        ctx.drawTextWithShadow(textRenderer, Text.literal(titleCase(selectedCategory.name())), startX, y + 12,
                0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.literal("left toggle / right settings"), startX, y + 26, 0xFF687789);
        drawSearchBar(ctx, startX, y + 42, contentW, mx, my, leftDown, accent);

        String filter = searchText.trim().toLowerCase(Locale.ROOT);
        List<Module> modules = ModuleManager.getByCategory(selectedCategory).stream()
                .filter(module -> filter.isEmpty() || module.getName().toLowerCase(Locale.ROOT).contains(filter))
                .toList();

        int totalHeight = modules.size() * 26;
        int visibleHeight = PANEL_H - 68;
        int maxScroll = Math.max(0, totalHeight - visibleHeight);
        if (modulesScroll > maxScroll) {
            modulesScroll = maxScroll;
        }

        // Only draw scrollbar if it actually overflows
        if (maxScroll > 0) {
            int scrollbarX = startX + contentW - 2;
            int scrollbarY = y + 68;
            int scrollbarH = PANEL_H - 78;
            int scrollbarW = 2;

            ctx.fill(scrollbarX, scrollbarY, scrollbarX + scrollbarW, scrollbarY + scrollbarH, 0x1AFFFFFF);

            double thumbRatio = (double) visibleHeight / totalHeight;
            int thumbH = (int) Math.max(10.0, scrollbarH * thumbRatio);
            double scrollRatio = modulesScroll / maxScroll;
            int thumbY = scrollbarY + (int) ((scrollbarH - thumbH) * scrollRatio);

            ctx.fill(scrollbarX, thumbY, scrollbarX + scrollbarW, thumbY + thumbH, accent);
        }

        boolean mouseInViewport = isInside(mx, my, startX, y + 68, contentW, PANEL_H - 68);
        double activeMx = mouseInViewport ? mx : -9999.0;
        double activeMy = mouseInViewport ? my : -9999.0;

        String tooltipText = null;
        int tooltipMx = 0, tooltipMy = 0;

        ctx.enableScissor(startX - 2, y + 66, startX + contentW + 2, y + PANEL_H);

        cy -= modulesScroll;

        for (Module module : modules) {
            boolean hovered = isInside(activeMx, activeMy, startX, cy, contentW - (maxScroll > 0 ? 6 : 0), 23);
            boolean enabled = module.isEnabled();
            int rowColor = enabled ? 0xFF142115 : 0xFF0F151D;

            ctx.fill(startX, cy, startX + contentW - (maxScroll > 0 ? 6 : 0), cy + 23, hovered ? 0xFF182231 : rowColor);
            ctx.fill(startX, cy, startX + 2, cy + 23, enabled ? accent : 0xFF263241);
            drawOutline(ctx, startX, cy, contentW - (maxScroll > 0 ? 6 : 0), 23, 0xFF1C2633);

            ctx.drawTextWithShadow(textRenderer, Text.literal(module.getName()), startX + 9, cy + 7,
                    enabled ? 0xFFE9FFE6 : 0xFFC4CAD3);

            String state = enabled ? "ON" : "OFF";
            int stateW = textRenderer.getWidth(state);
            int pillX = startX + contentW - (maxScroll > 0 ? 6 : 0) - 42;
            ctx.fill(pillX, cy + 5, pillX + 32, cy + 18, enabled ? 0xFF1F6B32 : 0xFF2A3441);
            ctx.drawTextWithShadow(textRenderer, Text.literal(state), pillX + 16 - stateW / 2, cy + 8, 0xFFFFFFFF);

            if (hovered && leftDown && !lastLeftDown) {
                module.toggle();
            } else if (hovered && rightDown && !lastRightDown) {
                settingsModule = module;
                listeningSetting = null;
                settingsScroll = 0.0;
            }

            if (hovered && !module.getDescription().isEmpty()) {
                tooltipText = module.getDescription();
                tooltipMx = (int) activeMx;
                tooltipMy = (int) activeMy;
            }

            cy += 26;
        }

        ctx.disableScissor();

        if (tooltipText != null) {
            drawTooltip(ctx, tooltipText, tooltipMx, tooltipMy);
        }
    }

    private void drawSearchBar(DrawContext ctx, int x, int y, int w, double mx, double my, boolean leftDown,
            int accent) {
        boolean hovered = isInside(mx, my, x, y, w, 18);
        if (leftDown && !lastLeftDown) {
            searchFocused = hovered;
        }

        ctx.fill(x, y, x + w, y + 18, searchFocused ? 0xFF152233 : 0xFF101821);
        drawOutline(ctx, x, y, w, 18, searchFocused ? accent : 0xFF263241);
        String label = searchText.isEmpty() && !searchFocused ? "Search modules" : searchText;
        ctx.drawTextWithShadow(textRenderer, Text.literal(label), x + 7, y + 5,
                searchText.isEmpty() ? 0xFF6E7A8A : 0xFFFFFFFF);
    }

    private void drawSettings(DrawContext ctx, int x, int y, double mx, double my, boolean leftDown, int accent) {
        int startX = x + SIDEBAR_W + 14;
        int contentW = PANEL_W - SIDEBAR_W - 28;
        int cy = y + 13;

        boolean backHovered = isInside(mx, my, startX, cy, 86, 16);
        ctx.fill(startX, cy, startX + 86, cy + 16, backHovered ? 0xFF182231 : 0xFF101821);
        ctx.drawTextWithShadow(textRenderer, Text.literal("< Back"), startX + 8, cy + 4, 0xFFB9C4D3);
        if (backHovered && leftDown && !lastLeftDown) {
            settingsModule = null;
            listeningSetting = null;
            settingsScroll = 0.0;
            return;
        }

        ctx.drawTextWithShadow(textRenderer, Text.literal(settingsModule.getName()), startX, y + 38, 0xFFFFFFFF);

        String desc = settingsModule.getDescription();
        if (!desc.isEmpty()) {
            ctx.drawTextWithShadow(textRenderer, Text.literal(desc), startX, y + 50, 0xFF8899AA);
            ctx.fill(startX, y + 64, startX + contentW, y + 65, 0xFF1E2A38);
        } else {
            ctx.fill(startX, y + 52, startX + contentW, y + 53, 0xFF1E2A38);
        }

        // Calculate total height of settings to clamp the scroll and draw scrollbar
        int totalHeight = 0;
        for (Setting<?> setting : settingsModule.getSettings()) {
            if (!setting.isVisible())
                continue;
            if (setting instanceof NumberSetting) {
                totalHeight += 32;
            } else if (setting instanceof ColorSetting) {
                totalHeight += 56;
            } else if (setting instanceof BooleanSetting) {
                totalHeight += 26;
            } else if (setting instanceof KeybindSetting) {
                totalHeight += 28;
            }
        }

        boolean hasDesc = !settingsModule.getDescription().isEmpty();
        int offset = hasDesc ? 12 : 0;
        int visibleHeight = PANEL_H - 61 - offset;
        int maxScroll = Math.max(0, totalHeight - visibleHeight);
        if (settingsScroll > maxScroll) {
            settingsScroll = maxScroll;
        }

        // Draw visual scrollbar if settings overflow
        if (maxScroll > 0) {
            int scrollbarX = startX + contentW - 2;
            int scrollbarY = y + 61 + offset;
            int scrollbarH = PANEL_H - 71 - offset;
            int scrollbarW = 2;

            ctx.fill(scrollbarX, scrollbarY, scrollbarX + scrollbarW, scrollbarY + scrollbarH, 0x1AFFFFFF);

            double thumbRatio = (double) visibleHeight / totalHeight;
            int thumbH = (int) Math.max(10.0, scrollbarH * thumbRatio);
            double scrollRatio = settingsScroll / maxScroll;
            int thumbY = scrollbarY + (int) ((scrollbarH - thumbH) * scrollRatio);

            ctx.fill(scrollbarX, thumbY, scrollbarX + scrollbarW, thumbY + thumbH, accent);
        }

        boolean mouseInViewport = isInside(mx, my, startX - 5, y + 54 + offset, contentW + 10, PANEL_H - 54 - offset);
        double activeMx = mouseInViewport ? mx : -9999.0;
        double activeMy = mouseInViewport ? my : -9999.0;

        ctx.enableScissor(startX - 5, y + 54 + offset, startX + contentW - (maxScroll > 0 ? 6 : 0), y + PANEL_H);

        cy = y + 61 + offset;
        cy -= settingsScroll;

        for (Setting<?> setting : settingsModule.getSettings()) {
            if (!setting.isVisible())
                continue;

            if (setting instanceof NumberSetting numberSetting) {
                cy = drawNumberSetting(ctx, numberSetting, startX, cy, contentW - (maxScroll > 0 ? 6 : 0), activeMx, activeMy, leftDown, accent);
            } else if (setting instanceof ColorSetting colorSetting) {
                cy = drawColorSetting(ctx, colorSetting, startX, cy, contentW - (maxScroll > 0 ? 6 : 0), activeMx, activeMy, leftDown);
            } else if (setting instanceof BooleanSetting booleanSetting) {
                cy = drawBooleanSetting(ctx, booleanSetting, startX, cy, contentW - (maxScroll > 0 ? 6 : 0), activeMx, activeMy, leftDown, accent);
            } else if (setting instanceof KeybindSetting keybindSetting) {
                cy = drawKeybindSetting(ctx, keybindSetting, startX, cy, contentW - (maxScroll > 0 ? 6 : 0), activeMx, activeMy, leftDown);
            }
        }

        ctx.disableScissor();
    }

    private int drawNumberSetting(DrawContext ctx, NumberSetting setting, int x, int y, int w, double mx, double my,
            boolean leftDown, int accent) {
        String value = formatNumber(setting.getValue());
        int valueW = textRenderer.getWidth(value);

        ctx.drawTextWithShadow(textRenderer, Text.literal(setting.getName()), x, y, 0xFFE5EBF2);
        ctx.drawTextWithShadow(textRenderer, Text.literal(value), x + w - valueW, y, 0xFF91A0B4);

        int sliderY = y + 14;
        int sliderH = 5;
        ctx.fill(x, sliderY, x + w, sliderY + sliderH, 0xFF202A36);

        double percent = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
        percent = Math.max(0.0, Math.min(1.0, percent));
        int fillW = (int) Math.round(w * percent);
        ctx.fill(x, sliderY, x + fillW, sliderY + sliderH, accent);

        int knobX = x + fillW;
        ctx.fill(knobX - 2, sliderY - 3, knobX + 3, sliderY + sliderH + 3, 0xFFFFFFFF);

        if (leftDown && isInside(mx, my, x, sliderY - 5, w, sliderH + 10)) {
            double newValue = setting.getMin() + ((mx - x) / w) * (setting.getMax() - setting.getMin());
            setting.setValue(Math.max(setting.getMin(), Math.min(setting.getMax(), newValue)));
        }

        return y + 32;
    }

    private int drawColorSetting(DrawContext ctx, ColorSetting setting, int x, int y, int w, double mx, double my,
            boolean leftDown) {
        int color = setting.getValue();
        String hex = String.format("#%06X", color & 0x00FFFFFF);
        int hexW = textRenderer.getWidth(hex);

        ctx.drawTextWithShadow(textRenderer, Text.literal(setting.getName()), x, y, 0xFFE5EBF2);
        ctx.fill(x + w - 38, y - 2, x + w - 18, y + 10, color);
        drawOutline(ctx, x + w - 38, y - 2, 20, 12, 0xFF2A3748);
        ctx.drawTextWithShadow(textRenderer, Text.literal(hex), x + w - hexW, y + 14, 0xFF91A0B4);

        int paletteY = y + 14;
        int paletteH = 30;
        for (int px = 0; px < w; px++) {
            float hue = px / (float) Math.max(1, w - 1);
            for (int py = 0; py < paletteH; py++) {
                float brightness = 1.0f - py / (float) Math.max(1, paletteH - 1);
                int swatch = java.awt.Color.HSBtoRGB(hue, 0.92f, Math.max(0.12f, brightness)) | 0xFF000000;
                ctx.fill(x + px, paletteY + py, x + px + 1, paletteY + py + 1, swatch);
            }
        }
        drawOutline(ctx, x, paletteY, w, paletteH, 0xFF263241);

        float[] hsb = java.awt.Color.RGBtoHSB(setting.getRed(), setting.getGreen(), setting.getBlue(), null);
        int markerX = x + Math.round(hsb[0] * (w - 1));
        int markerY = paletteY + Math.round((1.0f - hsb[2]) * (paletteH - 1));
        ctx.fill(markerX - 2, markerY - 2, markerX + 3, markerY + 3, 0xFFFFFFFF);
        drawOutline(ctx, markerX - 3, markerY - 3, 6, 6, 0xFF000000);

        if (leftDown && isInside(mx, my, x, paletteY, w, paletteH)) {
            float hue = (float) ((mx - x) / Math.max(1.0, w - 1));
            float brightness = 1.0f - (float) ((my - paletteY) / Math.max(1.0, paletteH - 1));
            hue = Math.max(0.0f, Math.min(1.0f, hue));
            brightness = Math.max(0.12f, Math.min(1.0f, brightness));
            setting.setValue(java.awt.Color.HSBtoRGB(hue, 0.92f, brightness) | 0xFF000000);
        }

        return y + 56;
    }

    private int drawBooleanSetting(DrawContext ctx, BooleanSetting setting, int x, int y, int w, double mx, double my,
            boolean leftDown, int accent) {
        boolean hovered = isInside(mx, my, x, y - 2, w, 22);
        boolean enabled = setting.getValue();

        if (hovered) {
            ctx.fill(x - 5, y - 4, x + w + 5, y + 19, 0xFF121B25);
        }

        ctx.drawTextWithShadow(textRenderer, Text.literal(setting.getName()), x, y + 4, 0xFFE5EBF2);

        int toggleX = x + w - 38;
        ctx.fill(toggleX, y + 2, toggleX + 34, y + 16, enabled ? 0xFF1F6B32 : 0xFF2A3441);
        ctx.fill(enabled ? toggleX + 20 : toggleX + 2, y + 4, enabled ? toggleX + 32 : toggleX + 14, y + 14,
                0xFFFFFFFF);
        if (enabled) {
            ctx.fill(toggleX, y + 2, toggleX + 2, y + 16, accent);
        }

        if (hovered && leftDown && !lastLeftDown) {
            setting.setValue(!enabled);
        }

        return y + 26;
    }

    private int drawKeybindSetting(DrawContext ctx, KeybindSetting setting, int x, int y, int w, double mx, double my,
            boolean leftDown) {
        String keyName = getKeyName(setting.getValue());
        if (listeningSetting == setting)
            keyName = "...";

        int buttonW = 70;
        int buttonX = x + w - buttonW;
        boolean hovered = isInside(mx, my, buttonX, y, buttonW, 18);

        ctx.drawTextWithShadow(textRenderer, Text.literal("Keybind"), x, y + 5, 0xFFE5EBF2);
        ctx.fill(buttonX, y, buttonX + buttonW, y + 18, hovered ? 0xFF1B2634 : 0xFF111923);
        drawOutline(ctx, buttonX, y, buttonW, 18, 0xFF2A3748);

        String label = keyName.toUpperCase();
        int labelW = textRenderer.getWidth(label);
        ctx.drawTextWithShadow(textRenderer, Text.literal(label), buttonX + buttonW / 2 - labelW / 2, y + 5,
                0xFFFFFFFF);

        if (hovered && leftDown && !lastLeftDown) {
            listeningSetting = setting;
        }

        return y + 28;
    }

    private void drawTooltip(DrawContext ctx, String text, int mx, int my) {
        int maxW = 220;
        List<String> lines = wrapText(text, maxW);
        int lineH = textRenderer.fontHeight + 2;
        int totalH = lines.size() * lineH + 4;
        int maxLineW = 0;
        for (String line : lines) {
            int lw = textRenderer.getWidth(line);
            if (lw > maxLineW) maxLineW = lw;
        }
        int bw = Math.min(maxLineW + 8, maxW + 8);
        int tx = mx + 10;
        int ty = my - 10;
        if (tx + bw > width) tx = mx - bw - 12;
        if (ty < 0) ty = my + 12;
        if (ty + totalH + 4 > height) ty = height - totalH - 6;
        ctx.fill(tx, ty - 2, tx + bw, ty + totalH + 2, 0xCC0B1017);
        ctx.fill(tx, ty - 2, tx + bw, ty + totalH + 2, 0x661B2430);
        drawOutline(ctx, tx, ty - 2, bw, totalH + 4, 0xFF2A3748);
        int ly = ty + 1;
        for (String line : lines) {
            ctx.drawTextWithShadow(textRenderer, Text.literal(line), tx + 4, ly, 0xFFCCD4DE);
            ly += lineH;
        }
    }

    private List<String> wrapText(String text, int maxPx) {
        List<String> lines = new ArrayList<>();
        if (textRenderer.getWidth(text) <= maxPx) {
            lines.add(text);
            return lines;
        }
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String test = current.isEmpty() ? word : current + " " + word;
            if (textRenderer.getWidth(test) > maxPx && !current.isEmpty()) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(test);
            }
        }
        if (!current.isEmpty()) lines.add(current.toString());
        return lines;
    }

    private void drawScaledText(DrawContext ctx, String text, int x, int y, float scale, int color) {
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(x, y);
        ctx.getMatrices().scale(scale, scale);
        ctx.drawTextWithShadow(textRenderer, Text.literal(text), 0, 0, color);
        ctx.getMatrices().popMatrix();
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

    private String titleCase(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    private String formatNumber(double value) {
        double rounded = Math.round(value * 100.0) / 100.0;
        if (rounded == Math.rint(rounded)) {
            return String.valueOf((int) rounded);
        }
        return String.valueOf(rounded);
    }

    public static String getKeyName(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return "NONE";
        
        switch (key) {
            case GLFW.GLFW_KEY_LEFT_SHIFT: return "LShift";
            case GLFW.GLFW_KEY_RIGHT_SHIFT: return "RShift";
            case GLFW.GLFW_KEY_LEFT_CONTROL: return "LCtrl";
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return "RCtrl";
            case GLFW.GLFW_KEY_LEFT_ALT: return "LAlt";
            case GLFW.GLFW_KEY_RIGHT_ALT: return "RAlt";
            case GLFW.GLFW_KEY_UP: return "Up";
            case GLFW.GLFW_KEY_DOWN: return "Down";
            case GLFW.GLFW_KEY_LEFT: return "Left";
            case GLFW.GLFW_KEY_RIGHT: return "Right";
            case GLFW.GLFW_KEY_SPACE: return "Space";
            case GLFW.GLFW_KEY_ESCAPE: return "Esc";
            case GLFW.GLFW_KEY_TAB: return "Tab";
            case GLFW.GLFW_KEY_BACKSPACE: return "BSpace";
            case GLFW.GLFW_KEY_ENTER: return "Enter";
            case GLFW.GLFW_KEY_KP_ENTER: return "NEnter";
            case GLFW.GLFW_KEY_DELETE: return "Del";
            case GLFW.GLFW_KEY_INSERT: return "Ins";
            case GLFW.GLFW_KEY_HOME: return "Home";
            case GLFW.GLFW_KEY_END: return "End";
            case GLFW.GLFW_KEY_PAGE_UP: return "PgUp";
            case GLFW.GLFW_KEY_PAGE_DOWN: return "PgDn";
            case GLFW.GLFW_KEY_CAPS_LOCK: return "Caps";
            case GLFW.GLFW_KEY_F1: return "F1";
            case GLFW.GLFW_KEY_F2: return "F2";
            case GLFW.GLFW_KEY_F3: return "F3";
            case GLFW.GLFW_KEY_F4: return "F4";
            case GLFW.GLFW_KEY_F5: return "F5";
            case GLFW.GLFW_KEY_F6: return "F6";
            case GLFW.GLFW_KEY_F7: return "F7";
            case GLFW.GLFW_KEY_F8: return "F8";
            case GLFW.GLFW_KEY_F9: return "F9";
            case GLFW.GLFW_KEY_F10: return "F10";
            case GLFW.GLFW_KEY_F11: return "F11";
            case GLFW.GLFW_KEY_F12: return "F12";
            default:
                String name = GLFW.glfwGetKeyName(key, 0);
                return name != null ? name.toUpperCase() : "KEY " + key;
        }
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        int key = input.key();
        if (listeningSetting instanceof KeybindSetting keybindSetting) {
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE) {
                keybindSetting.setValue(GLFW.GLFW_KEY_UNKNOWN);
            } else {
                keybindSetting.setValue(key);
            }
            listeningSetting = null;
            return true;
        }

        if (searchFocused) {
            if (key == GLFW.GLFW_KEY_BACKSPACE && !searchText.isEmpty()) {
                searchText = searchText.substring(0, searchText.length() - 1);
                return true;
            }
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                if (!searchText.isEmpty()) {
                    searchText = "";
                } else {
                    searchFocused = false;
                }
                return true;
            }
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                searchFocused = false;
                return true;
            }
        }

        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT || key == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!searchFocused)
            return super.charTyped(input);
        if (searchText.length() >= 32)
            return true;
        if (input.isValidChar()) {
            searchText += input.asString();
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (settingsModule != null) {
            settingsScroll -= verticalAmount * 15;
            if (settingsScroll < 0) {
                settingsScroll = 0.0;
            }
        } else {
            modulesScroll -= verticalAmount * 15;
            if (modulesScroll < 0) {
                modulesScroll = 0.0;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected void init() {
        super.init();
        com.example.liquorclient.render.ClickGuiMod mod = com.example.liquorclient.render.ClickGuiMod.get();
        if (mod != null && !mod.isEnabled()) {
            mod.setEnabledSilently(true);
        }
    }

    @Override
    public void close() {
        super.close();
        com.example.liquorclient.render.ClickGuiMod mod = com.example.liquorclient.render.ClickGuiMod.get();
        if (mod != null && mod.isEnabled()) {
            mod.setEnabledSilently(false);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
