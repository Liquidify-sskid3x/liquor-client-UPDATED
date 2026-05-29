package com.example.liquorclient;

import com.example.liquorclient.config.ConfigManager;
import com.example.liquorclient.gui.NotificationManager;
import com.example.liquorclient.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;

public class LiquorClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        com.example.liquorclient.gui.ESPManager.init();
        ModuleManager.init();
        ConfigManager.init();
        com.example.liquorclient.gui.ArrayListHud.init();
        NotificationManager.init();
        Keybinds.register();
    }
}
