package com.example.liquorclient.utility;

import com.example.liquorclient.render.ClickGuiMod;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.util.Identifier;

public class CustomFontRenderer {
    private static final Identifier[] FONT_IDS = {
        Identifier.ofVanilla("default"),
        Identifier.of("liquor", "orbitron"),
        Identifier.of("liquor", "chakra_petch"),
        Identifier.of("liquor", "poppins")
    };

    public static int getCurrentFace() {
        ClickGuiMod mod = ClickGuiMod.get();
        if (mod == null) return 0;
        return (int) mod.getFontFace();
    }

    public static Identifier getCurrentFontId() {
        int face = Math.max(0, Math.min(3, getCurrentFace()));
        return FONT_IDS[face];
    }

    public static Text withFont(String text) {
        MutableText t = Text.literal(text);
        int face = getCurrentFace();
        if (face != 0) {
            StyleSpriteSource fontSource = new StyleSpriteSource.Font(FONT_IDS[face]);
            t = t.styled(s -> s.withFont(fontSource));
        }
        return t;
    }

    public static Text withFont(String text, int fontIndex) {
        MutableText t = Text.literal(text);
        int idx = Math.max(0, Math.min(3, fontIndex));
        if (idx != 0) {
            StyleSpriteSource fontSource = new StyleSpriteSource.Font(FONT_IDS[idx]);
            t = t.styled(s -> s.withFont(fontSource));
        }
        return t;
    }

    public static void drawString(DrawContext ctx, TextRenderer renderer, String text, int x, int y, int color) {
        ctx.drawTextWithShadow(renderer, withFont(text), x, y, color);
    }

    public static void drawText(DrawContext ctx, TextRenderer renderer, String text, int x, int y, int color, boolean shadow) {
        ctx.drawText(renderer, withFont(text), x, y, color, shadow);
    }

    public static int getStringWidth(TextRenderer renderer, String text) {
        return renderer.getWidth(withFont(text));
    }

    public static int getFontHeight() {
        return 9;
    }
}
