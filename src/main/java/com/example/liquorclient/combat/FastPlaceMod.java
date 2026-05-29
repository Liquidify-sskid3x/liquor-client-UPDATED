package com.example.liquorclient.combat;

import com.example.liquorclient.mixin.MinecraftClientAccessor;
import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.NumberSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class FastPlaceMod extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private final NumberSetting cooldown = new NumberSetting("Cooldown", 0.0, 0.0, 4.0);

    public FastPlaceMod() {
        super("FastPlace", Category.PLAYER, "Removes block placement delay");
        addSetting(cooldown);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isEnabled() || mc.player == null || mc.currentScreen != null) return;
            int value = Math.max(0, Math.min(4, (int) Math.round(cooldown.getValue())));
            ((MinecraftClientAccessor) mc).liquor$setItemUseCooldown(value);
        });
    }
}
