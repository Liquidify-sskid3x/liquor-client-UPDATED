package com.example.liquorclient.utility;

import com.example.liquorclient.gui.ArrayListHud;
import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ArmorHudMod extends Module {
    public ArmorHudMod() {
        super("Armor HUD", Category.RENDER, "Shows your equipped armor status");

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> render(ctx));
    }

    private void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!isEnabled() || mc.player == null || mc.options.hudHidden) return;

        int drawX = ArrayListHud.getArmorX();
        int drawY = ArrayListHud.getArmorY();
        EquipmentSlot[] slots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };

        ctx.drawTextWithShadow(mc.textRenderer, Text.literal("Armor"), drawX, drawY, 0xFFFFFFFF);
        drawY += 12;

        for (EquipmentSlot slot : slots) {
            ItemStack stack = mc.player.getEquippedStack(slot);
            if (stack.isEmpty()) continue;

            ctx.drawItem(stack, drawX, drawY);
            String label = durabilityText(stack);
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal(label), drawX + 20, drawY + 5, durabilityColor(stack));
            drawY += 18;
        }
    }

    private String durabilityText(ItemStack stack) {
        if (stack.getMaxDamage() <= 0) return "x" + stack.getCount();
        int remaining = stack.getMaxDamage() - stack.getDamage();
        int pct = Math.round((remaining / (float) stack.getMaxDamage()) * 100.0f);
        return pct + "%";
    }

    private int durabilityColor(ItemStack stack) {
        if (stack.getMaxDamage() <= 0) return 0xFFE5EBF2;
        float pct = (stack.getMaxDamage() - stack.getDamage()) / (float) stack.getMaxDamage();
        if (pct > 0.55f) return 0xFF55FF55;
        if (pct > 0.25f) return 0xFFFFFF55;
        return 0xFFFF5555;
    }
}
