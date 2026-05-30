package com.example.liquorclient.combat;

import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.ModuleManager;
import com.example.liquorclient.module.NumberSetting;

import java.util.concurrent.ThreadLocalRandom;

public class ReachMod extends Module {
    private final NumberSetting range = new NumberSetting("Range", 4.0, 3.0, 6.0);
    private final NumberSetting chance = new NumberSetting("Chance", 100.0, 0.0, 100.0);

    public ReachMod() {
        super("Reach", Category.COMBAT, "Extends your attack reach distance");
        addSetting(range);
        addSetting(chance);
    }

    public static double getEffectiveRange(double vanillaRange) {
        ReachMod module = ModuleManager.getModule(ReachMod.class);
        if (module == null || !module.isEnabled()) return vanillaRange;

        double roll = ThreadLocalRandom.current().nextDouble(100.0);
        if (roll > module.chance.getValue()) return vanillaRange;

        return Math.max(vanillaRange, module.range.getValue());
    }
}
