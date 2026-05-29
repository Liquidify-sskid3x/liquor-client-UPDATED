package com.example.liquorclient.player;

import com.example.liquorclient.module.BooleanSetting;
import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class SprintMod extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final BooleanSetting checkHunger = new BooleanSetting("Check Hunger", true);
    private final BooleanSetting multiDirection = new BooleanSetting("Multi Direction", false);

    public SprintMod() {
        super("Sprint", Category.MOVEMENT, "Auto-sprint with hunger check and multi-direction support");
        addSetting(checkHunger);
        addSetting(multiDirection);

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (!isEnabled() || mc.player == null) return;
            if (!multiDirection.getValue() && mc.player.input.getMovementInput().y == 0f && mc.player.input.getMovementInput().x > 0f) {
                mc.player.setSprinting(true);
                return;
            }
            if (mc.player.input.getMovementInput().x > 0f || multiDirection.getValue() && (mc.player.input.getMovementInput().y != 0f || mc.player.input.getMovementInput().x > 0f)) {
                if (!checkHunger.getValue() || mc.player.getHungerManager().getFoodLevel() > 6) {
                    mc.player.setSprinting(true);
                }
            }
        });
    }
}
