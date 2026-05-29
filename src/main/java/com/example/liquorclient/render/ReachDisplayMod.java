package com.example.liquorclient.render;

import com.example.liquorclient.module.BooleanSetting;
import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.NumberSetting;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class ReachDisplayMod extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final NumberSetting xPos = new NumberSetting("X", 10.0, 0.0, 2000.0);
    private final NumberSetting yPos = new NumberSetting("Y", 10.0, 0.0, 2000.0);
    private final BooleanSetting showOnHit = new BooleanSetting("Show On Hit", false);

    private double lastDistance = 0;
    private long lastHitTime = 0;

    public ReachDisplayMod() {
        super("Reach Display", Category.RENDER, "Shows your attack distance on screen");
        addSetting(xPos);
        addSetting(yPos);
        addSetting(showOnHit);

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            if (!isEnabled() || mc.player == null) return;
            if (lastDistance == 0) return;
            long elapsed = System.currentTimeMillis() - lastHitTime;
            if (showOnHit.getValue() && elapsed > 3000) return;
            String text = "Reach: " + String.format("%.2f", lastDistance) + " blocks";
            ctx.drawText(mc.textRenderer, Text.literal(text), (int) (double) xPos.getValue(), (int) (double) yPos.getValue(), 0xFFFFFF, true);
        });
    }

    public void onAttack(double distance) {
        lastDistance = distance;
        lastHitTime = System.currentTimeMillis();
    }
}
