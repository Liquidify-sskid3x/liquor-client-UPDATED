package com.example.liquorclient.render;

import com.example.liquorclient.module.BooleanSetting;
import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class BetterTooltipsMod extends Module {
    private final BooleanSetting durability = new BooleanSetting("Durability", true);
    private final BooleanSetting enchants = new BooleanSetting("Enchants", true);
    private final BooleanSetting itemId = new BooleanSetting("Item ID", false);

    public BetterTooltipsMod() {
        super("Better Tooltips", Category.RENDER, "Shows durability, enchantments, and item IDs on tooltips");
        addSetting(durability);
        addSetting(enchants);
        addSetting(itemId);

        ItemTooltipCallback.EVENT.register((stack, ctx, type, lines) -> {
            if (!isEnabled()) return;

            if (durability.getValue() && stack.isDamageable()) {
                int maxDamage = stack.getMaxDamage();
                int damage = stack.getDamage();
                int remaining = maxDamage - damage;
                if (maxDamage > 0) {
                    int pct = (int) ((double) remaining / maxDamage * 100);
                    lines.add(Text.literal("Durability: " + remaining + "/" + maxDamage + " (" + pct + "%)").formatted(Formatting.GRAY));
                } else {
                    lines.add(Text.literal("Durability: " + remaining + "/" + maxDamage).formatted(Formatting.GRAY));
                }
            }

            if (enchants.getValue()) {
                var enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
                if (enchantments != null) {
                    var enchList = enchantments.getEnchantments();
                    if (!enchList.isEmpty()) {
                        lines.add(Text.literal("Enchantments (" + enchList.size() + "):").formatted(Formatting.AQUA));
                        for (var entry : enchList) {
                            int level = enchantments.getLevel(entry);
                            lines.add(Text.literal(" - ").append(entry.value().getName(entry, level)).formatted(Formatting.GRAY));
                        }
                    }
                }
            }

            if (itemId.getValue()) {
                lines.add(Text.literal("ID: " + stack.getItem().toString()).formatted(Formatting.DARK_GRAY));
            }
        });
    }
}
