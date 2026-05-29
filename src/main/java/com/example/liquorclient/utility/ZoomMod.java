package com.example.liquorclient.utility;

import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.NumberSetting;
import com.example.liquorclient.module.ModuleManager;

public class ZoomMod extends Module {
    private final NumberSetting factor = new NumberSetting("Factor", 3.0, 1.5, 10.0, 0.5);

    public ZoomMod() {
        super("Zoom", Category.RENDER, "Zooms your view in and out");
        addSetting(factor);
    }

    public static float apply(float fov) {
        ZoomMod zoom = ModuleManager.getModule(ZoomMod.class);
        if (zoom == null || !zoom.isEnabled()) return fov;
        return (float) Math.max(1.0, fov / Math.max(1.0, zoom.factor.getValue()));
    }
}
