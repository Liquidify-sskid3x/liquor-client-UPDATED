package com.example.liquorclient.combat;

import com.example.liquorclient.mixin.MinecraftClientAccessor;
import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.NumberSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.concurrent.ThreadLocalRandom;

public class RightClickerMod extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private final NumberSetting minCps = new NumberSetting("Min CPS", 8.0, 1.0, 25.0);
    private final NumberSetting maxCps = new NumberSetting("Max CPS", 12.0, 1.0, 25.0);
    private long nextClickTime = 0L;

    public RightClickerMod() {
        super("Right Clicker", Category.COMBAT, "Automatically right-clicks when holding items");
        addSetting(minCps);
        addSetting(maxCps);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isEnabled() || mc.player == null || mc.interactionManager == null || mc.currentScreen != null) return;
            if (!mc.options.useKey.isPressed()) {
                nextClickTime = 0L;
                return;
            }

            long now = System.currentTimeMillis();
            if (now < nextClickTime) return;

            MinecraftClientAccessor accessor = (MinecraftClientAccessor) mc;
            accessor.liquor$setItemUseCooldown(0);
            accessor.liquor$doItemUse();
            nextClickTime = now + cpsDelay();
        });
    }

    private long cpsDelay() {
        double min = Math.max(1.0, minCps.getValue());
        double max = Math.max(1.0, maxCps.getValue());
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }

        double cps = min == max ? min : ThreadLocalRandom.current().nextDouble(min, max);
        return Math.max(1L, Math.round(1000.0 / cps));
    }
}
