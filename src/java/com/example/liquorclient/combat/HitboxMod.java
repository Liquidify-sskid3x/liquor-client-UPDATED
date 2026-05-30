package com.example.liquorclient.combat;

import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.NumberSetting;

public class HitboxMod extends Module {
    private final NumberSetting expand = new NumberSetting("Expand", 0.1, 0.0, 1.0);

    public HitboxMod() {
        super("Hitbox", Category.COMBAT, "Expands entity hitboxes for easier hits");
        addSetting(expand);
    }

    public float getExpansion() {
        return isEnabled() ? expand.getValue().floatValue() : 0.0f;
    }
}
