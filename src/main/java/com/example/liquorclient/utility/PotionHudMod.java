package com.example.liquorclient.utility;

import com.example.liquorclient.gui.ArrayListHud;
import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;

public class PotionHudMod extends Module {
    public PotionHudMod() {
        super("Potion HUD", Category.RENDER, "Shows your active potion effects");

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> render(ctx));
    }

    private void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!isEnabled() || mc.player == null || mc.options.hudHidden) return;
        if (mc.player.getStatusEffects().isEmpty()) return;

        int drawX = ArrayListHud.getPotionX();
        int drawY = ArrayListHud.getPotionY();
        ctx.drawTextWithShadow(mc.textRenderer, Text.literal("Effects"), drawX, drawY, 0xFFFFFFFF);
        drawY += 12;

        for (StatusEffectInstance effect : mc.player.getStatusEffects()) {
            String name = effect.getEffectType().value().getName().getString();
            if (effect.getAmplifier() > 0) {
                name += " " + (effect.getAmplifier() + 1);
            }
            String line = name + " " + duration(effect);
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal(line), drawX, drawY, 0xFFE5EBF2);
            drawY += 11;
        }
    }

    private String duration(StatusEffectInstance effect) {
        if (effect.isInfinite()) return "inf";
        int totalSeconds = Math.max(0, effect.getDuration() / 20);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }
}
