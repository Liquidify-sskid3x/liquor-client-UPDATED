package com.example.liquorclient.render;

import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.BooleanSetting;
import com.example.liquorclient.module.NumberSetting;

public class ItemEspMod extends Module {
    private final NumberSetting range = new NumberSetting("Range", 64.0, 10.0, 256.0);
    private final BooleanSetting names = new BooleanSetting("Names", true);

    public ItemEspMod() {
        super("Item ESP", Category.RENDER, "Highlights dropped items through walls");
        addSetting(range);
        addSetting(names);
    }

    public double getRange() {
        return range.getValue();
    }

    public boolean shouldShowNames() {
        return names.getValue();
    }
}
