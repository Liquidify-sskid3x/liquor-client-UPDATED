package com.example.liquorclient.developer;

import com.example.liquorclient.gui.DeveloperScreen;
import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import net.minecraft.client.MinecraftClient;

public class DeveloperPanelMod extends Module {
    public DeveloperPanelMod() {
        super("Developer Panel", Category.DEVELOPER, "Developer debugging and testing tools");
    }

    @Override
    public void toggle() {
        open();
    }

    @Override
    protected void onEnable() {
        open();
        setEnabledSilently(false);
    }

    private void open() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) {
            mc.setScreen(new DeveloperScreen(mc.currentScreen));
        }
    }
}
